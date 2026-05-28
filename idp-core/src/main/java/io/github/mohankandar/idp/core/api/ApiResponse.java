package io.github.mohankandar.idp.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Minimal, framework-agnostic API envelope.
 * - Success: status="ok", data!=null
 * - Error:   status="error", error!=null
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final String status;          // "ok" | "error"
    private final T data;                 // present on success
    private final ErrorDetail error;      // present on error
    private final String correlationId;   // propagated by platform filter
    private final Instant timestamp;      // server-side timestamp

    private ApiResponse(String status, T data, ErrorDetail error, String correlationId, Instant timestamp) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.correlationId = correlationId;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("ok", data, null, currentCorrelationId(), Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String correlationId) {
        return new ApiResponse<>("ok", data, null, correlationId, Instant.now());
    }

    public static <T> ApiResponse<T> error(ErrorDetail error) {
        Objects.requireNonNull(error, "error");
        return new ApiResponse<>("error", null, error, currentCorrelationId(), Instant.now());
    }

    public static <T> ApiResponse<T> error(ErrorDetail error, String correlationId) {
        Objects.requireNonNull(error, "error");
        return new ApiResponse<>("error", null, error, correlationId, Instant.now());
    }

    public String getStatus() { return status; }
    public T getData() { return data; }
    public ErrorDetail getError() { return error; }
    public String getCorrelationId() { return correlationId; }
    public Instant getTimestamp() { return timestamp; }

    /** Optional JSON helper for logging/tests (not required at runtime). */
    public String toJson() {
        try { return MAPPER.writeValueAsString(this); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize ApiResponse", e); }
    }

    public Optional<T> dataOpt() { return Optional.ofNullable(data); }
    public Optional<ErrorDetail> errorOpt() { return Optional.ofNullable(error); }
    private static String currentCorrelationId() {
        String correlationId = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
        return (correlationId == null || correlationId.isBlank()) ? null : correlationId;
    }

}
