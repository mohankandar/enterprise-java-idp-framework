package io.github.mohankandar.idp.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

public class ApiKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final SecurityProperties props;

  @SuppressFBWarnings(
          value = "EI_EXPOSE_REP2",
          justification = "SecurityProperties is a framework configuration holder intentionally retained for request-time evaluation."
  )
  public ApiKeyAuthFilter(SecurityProperties props) {
    this.props = props;
    setCheckForPrincipalChanges(false);
    setContinueFilterChainOnUnsuccessfulAuthentication(true);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    String authz = request.getHeader("Authorization");
    if (authz != null && authz.startsWith("Bearer ")) {
      return null;
    }

    var apiKey = props.getApiKey();
    if (apiKey == null || !apiKey.isEnabled()) {
      return null;
    }

    String headerName = StringUtils.hasText(apiKey.getHeader()) ? apiKey.getHeader() : "X-API-Key";
    String presented = request.getHeader(headerName);
    if (!StringUtils.hasText(presented)) {
      return null;
    }

    Map<String, SecurityProperties.ApiKey.Client> clients = apiKey.getClients();
    if (clients != null && !clients.isEmpty()) {
      for (var entry : clients.entrySet()) {
        String clientId = entry.getKey();
        List<String> keys = entry.getValue() != null ? entry.getValue().getKeys() : null;
        if (keys == null) {
          continue;
        }

        for (String expected : keys) {
          if (secureEquals(expected, presented)) {
            return clientId;
          }
        }
      }
      return null;
    }

    String expected = apiKey.getValue();
    if (!StringUtils.hasText(expected)) {
      return null;
    }
    return secureEquals(expected, presented) ? "API-KEY" : null;
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

  static boolean secureEquals(String expected, String presented) {
    if (!StringUtils.hasText(expected) || !StringUtils.hasText(presented)) return false;
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        presented.getBytes(StandardCharsets.UTF_8)
    );
  }
}
