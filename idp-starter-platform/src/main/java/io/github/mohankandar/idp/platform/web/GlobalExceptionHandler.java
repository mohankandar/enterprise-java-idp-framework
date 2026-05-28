package io.github.mohankandar.idp.platform.web;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.core.error.IdpException;
import io.github.mohankandar.idp.core.logging.IdpLogger;
import io.github.mohankandar.idp.core.logging.IdpLoggerFactory;
import io.github.mohankandar.idp.platform.logging.CorrelationIdFilter;
import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@RestControllerAdvice
@Order(HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final IdpLogger log = IdpLoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", fieldErrors);

        ErrorDetail error = ErrorDetail.of(
                ErrorCode.VALIDATION_ERROR,
                "Request validation failed.",
                details
        );

        return toResponse(HttpStatus.BAD_REQUEST, error, ex, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("violations", violations);

        ErrorDetail error = ErrorDetail.of(
                ErrorCode.CONSTRAINT_VIOLATION,
                "Constraint violation.",
                details
        );

        return toResponse(HttpStatus.BAD_REQUEST, error, ex, request);
    }

    @ExceptionHandler(IdpException.class)
    public ResponseEntity<ApiResponse<Void>> handleIdpException(IdpException ex, HttpServletRequest request) {
        ErrorDetail error = ex.getError();
        if (error == null) {
            error = ErrorDetail.of(
                    ErrorCode.UNEXPECTED_ERROR,
                    "An unexpected error occurred. Please contact support if this persists."
            );
        }

        HttpStatus status = resolveStatus(ex);
        return toResponse(status, error, ex, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorDetail error = ErrorDetail.of(ErrorCode.NOT_FOUND, "Route not found.");
        return toResponse(HttpStatus.NOT_FOUND, error, ex, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorDetail error = ErrorDetail.of(
                resolveErrorCode(status),
                ex.getReason() != null ? ex.getReason() : status.getReasonPhrase()
        );

        return toResponse(status, error, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex, HttpServletRequest request) {
        ErrorDetail error = ErrorDetail.of(
                ErrorCode.UNEXPECTED_ERROR,
                "An unexpected error occurred. Please contact support if this persists."
        );

        return toResponse(HttpStatus.INTERNAL_SERVER_ERROR, error, ex, request);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(
            HttpStatus status,
            ErrorDetail error,
            Exception ex,
            HttpServletRequest request
    ) {
        String correlationId = resolveCorrelationId(request);
        String method = request != null ? request.getMethod() : "";
        String path = request != null ? request.getRequestURI() : "";
        String code = error != null ? error.getCode() : ErrorCode.UNEXPECTED_ERROR.getCode();
        String message = error != null ? error.getMessage() : "Unexpected error";
        String exceptionType = ex != null ? ex.getClass().getSimpleName() : "UnknownException";

        if (status.is4xxClientError()) {
            log.warn("Handled exception status={} code={} exceptionType={} corrId={} method={} path={} message={}",
                    status.value(), code, exceptionType, correlationId, method, path, message);
        } else {
            log.error("Unhandled exception status={} code={} exceptionType={} corrId={} method={} path={} message={}",
                    ex,
                    status.value(), code, exceptionType, correlationId, method, path, message);
        }

        ApiResponse<Void> body = ApiResponse.error(error, correlationId);

        return ResponseEntity.status(status)
                .header(CorrelationIdFilter.HDR, correlationId)
                .body(body);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String fromMdc = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }

        if (request != null) {
            String fromHeader = request.getHeader(CorrelationIdFilter.HDR);
            if (fromHeader != null && !fromHeader.isBlank()) {
                MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, fromHeader);
                return fromHeader;
            }
        }

        String generated = UUID.randomUUID().toString();
        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, generated);
        return generated;
    }

    private static HttpStatus resolveStatus(IdpException ex) {
        if (ex.getHttpStatus() != null) {
            try {
                return HttpStatus.valueOf(ex.getHttpStatus());
            } catch (Exception ignored) {
            }
        }

        String code = ex.getError() != null ? ex.getError().getCode() : null;
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (ErrorCode.VALIDATION_ERROR.getCode().equals(code)
                || ErrorCode.CONSTRAINT_VIOLATION.getCode().equals(code)
                || ErrorCode.BAD_REQUEST.getCode().equals(code)) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ErrorCode.UNAUTHORIZED.getCode().equals(code)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ErrorCode.FORBIDDEN.getCode().equals(code)) {
            return HttpStatus.FORBIDDEN;
        }
        if (ErrorCode.NOT_FOUND.getCode().equals(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if (ErrorCode.CONFLICT.getCode().equals(code)) {
            return HttpStatus.CONFLICT;
        }
        if (ErrorCode.TOO_MANY_REQUESTS.getCode().equals(code)) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static ErrorCode resolveErrorCode(HttpStatus status) {
        if (status == HttpStatus.BAD_REQUEST) {
            return ErrorCode.BAD_REQUEST;
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            return ErrorCode.UNAUTHORIZED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return ErrorCode.FORBIDDEN;
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ErrorCode.NOT_FOUND;
        }
        if (status == HttpStatus.CONFLICT) {
            return ErrorCode.CONFLICT;
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return ErrorCode.TOO_MANY_REQUESTS;
        }
        return ErrorCode.UNEXPECTED_ERROR;
    }
}
