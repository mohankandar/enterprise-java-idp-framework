package io.github.mohankandar.idp.platform.logging;
import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MdcUserEnricherFilter extends OncePerRequestFilter {

  private static final String MDC_USER = IdpLoggingConstants.MDC_USER;

  @Override
  protected void doFilterInternal(HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain)
      throws ServletException, IOException {

    final String previousUser = MDC.get(MDC_USER);
    final String resolvedUser = resolveUser();

    if (resolvedUser != null && !resolvedUser.isBlank()) {
      MDC.put(MDC_USER, resolvedUser);
    } else {
      MDC.remove(MDC_USER);
    }

    try {
      chain.doFilter(req, res);
    } finally {
      if (previousUser == null || previousUser.isBlank()) {
        MDC.remove(MDC_USER);
      } else {
        MDC.put(MDC_USER, previousUser);
      }
    }
  }

  private String resolveUser() {
    Authentication auth = SecurityContextHolder.getContext() != null
        ? SecurityContextHolder.getContext().getAuthentication()
        : null;

    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }

    String user = null;

    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      Jwt jwt = (Jwt) jwtAuth.getPrincipal();
      user = firstNonBlank(
          jwt.getClaimAsString("userID"),
          jwt.getClaimAsString("userId"),
          jwt.getSubject(),
          jwt.getClaimAsString("preferred_username"),
          jwt.getClaimAsString("name")
      );
    }

    if (user == null) {
      user = blankToNull(auth.getName());
    }

    return user;
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
