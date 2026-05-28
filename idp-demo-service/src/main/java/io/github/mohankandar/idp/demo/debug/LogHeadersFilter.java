package io.github.mohankandar.idp.demo.debug;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // run before security filters
@ConditionalOnProperty(prefix = "idp.debug", name = "log-headers", havingValue = "true")
public class LogHeadersFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(LogHeadersFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    // only dump for our API calls; adjust as you like
    String path = request.getRequestURI();
    if (path.startsWith("/api/")) {
      StringBuilder sb = new StringBuilder(256);
      sb.append("Request ").append(request.getMethod()).append(" ").append(path).append(" — headers:");
      Enumeration<String> names = request.getHeaderNames();
      for (String name : Collections.list(names)) {
        sb.append("\n  ").append(name).append(": ").append(request.getHeader(name));
      }
      log.debug(sb.toString());
    }
    filterChain.doFilter(request, response);
  }
}
