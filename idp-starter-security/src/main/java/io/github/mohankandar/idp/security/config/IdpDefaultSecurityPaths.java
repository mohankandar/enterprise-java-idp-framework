package io.github.mohankandar.idp.security.config;

import java.util.List;

public final class IdpDefaultSecurityPaths {

  public static final List<String> DEFAULT_PERMIT_ALL = List.of(
          "/actuator/health",
          "/actuator/info",
          "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html",
          "/api/token"
  );

  private IdpDefaultSecurityPaths() {
  }
}
