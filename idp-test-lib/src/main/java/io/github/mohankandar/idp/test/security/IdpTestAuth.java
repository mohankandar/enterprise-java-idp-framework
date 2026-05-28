package io.github.mohankandar.idp.test.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/** Common request post-processors for IDP security tests. */
public final class IdpTestAuth {

  private IdpTestAuth() {}

  /** Default IDP API key header name used by {@code ApiKeyAuthFilter}. */
  public static final String DEFAULT_API_KEY_HEADER = "X-API-Key";

  public static RequestPostProcessor apiKey(String apiKeyValue) {
    return request -> {
      request.addHeader(DEFAULT_API_KEY_HEADER, apiKeyValue);
      return request;
    };
  }

  public static RequestPostProcessor apiKey(String headerName, String apiKeyValue) {
    return request -> {
      request.addHeader(headerName, apiKeyValue);
      return request;
    };
  }

  /** Minimal JWT request post-processor; add claims in the customizer as needed. */
  public static RequestPostProcessor jwtAuth() {
    return jwt();
  }

  public static RequestPostProcessor jwtAuth(Jwt jwt) {
    return jwt().jwt(jwt);
  }
}
