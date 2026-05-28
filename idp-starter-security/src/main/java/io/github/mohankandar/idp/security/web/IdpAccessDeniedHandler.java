package io.github.mohankandar.idp.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class IdpAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @SuppressFBWarnings(
          value = "EI_EXPOSE_REP2",
          justification = "Injected ObjectMapper is a framework-managed collaborator reference."
  )
  public IdpAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException
  ) throws IOException, ServletException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("path", request.getRequestURI());
    details.put("method", request.getMethod());

    String correlationId = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
    if (correlationId != null && !correlationId.isBlank()) {
      details.put("correlationId", correlationId);
      response.setHeader(IdpLoggingConstants.CORRELATION_HEADER, correlationId);
    }

    String message = "Forbidden";
    ApiResponse<Void> body = ApiResponse.error(ErrorDetail.of(ErrorCode.FORBIDDEN, message, details));

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
