package io.github.mohankandar.idp.platform.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class LogHeadersFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(LogHeadersFilter.class);

  private final Set<String> allowedHeaders;

  public LogHeadersFilter(Set<String> allowedHeaders) {
      this.allowedHeaders = allowedHeaders == null ? Set.of() : Set.copyOf(allowedHeaders);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;

    Map<String, String> headersToLog = new LinkedHashMap<>();
    Collections.list(req.getHeaderNames()).forEach(name -> {
      if (allowedHeaders.contains(name.toLowerCase(Locale.ROOT))) {
        headersToLog.put(name, req.getHeader(name));
      }
    });

    if (!headersToLog.isEmpty()) {
      StringBuilder sb = new StringBuilder("Request ")
          .append(req.getMethod()).append(' ').append(req.getRequestURI()).append(" — allowed headers:");
      headersToLog.forEach((name, value) -> sb.append(" ").append(name).append(": ").append(value));
      log.debug(sb.toString());
    }

    chain.doFilter(request, response);
  }
}
