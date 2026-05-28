package io.github.mohankandar.idp.platform.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Global HTTP client configuration for IDP.
 * <p>
 * Provides opinionated defaults for connect/read timeouts and allows
 * optional per-client overrides.
 */
@ConfigurationProperties(prefix = "idp.http")
public class IdpHttpProperties {

  /**
   * Global timeout settings applied to all HTTP clients by default.
   */
  private TimeoutProperties timeout = new TimeoutProperties();

  /**
   * Optional per-client overrides. The key is a logical client name,
   * e.g. "billing-service" or "partner-api".
   *
   * For Feign, we recommend using the Feign client name here.
   */
  private Map<String, ClientProperties> clients = new HashMap<>();

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
  public TimeoutProperties getTimeout() {
    return timeout;
  }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
    )
  public void setTimeout(TimeoutProperties timeout) {
        this.timeout = timeout == null ? new TimeoutProperties() : timeout;
  }

  public Map<String, ClientProperties> getClients() {
      return Map.copyOf(clients);
  }

  public void setClients(Map<String, ClientProperties> clients) {
      this.clients = clients == null ? new HashMap<>() : new HashMap<>(clients);
  }

  /**
   * Resolve the effective timeout configuration for a given client name,
   * falling back to the global defaults when no override is defined.
   */
  @SuppressFBWarnings(
          value = "EI_EXPOSE_REP",
          justification = "Spring @ConfigurationProperties holder intentionally returns nested mutable timeout bean for client-specific override resolution."
  )
  public TimeoutProperties resolveTimeoutForClient(String clientName) {
    ClientProperties clientProps = clients.get(clientName);
    if (clientProps == null || clientProps.getTimeout() == null) {
      return timeout;
    }
    TimeoutProperties override = clientProps.getTimeout();

    TimeoutProperties merged = new TimeoutProperties();
    merged.setConnectMs(
        override.getConnectMs() != null ? override.getConnectMs() : timeout.getConnectMs());
    merged.setReadMs(
        override.getReadMs() != null ? override.getReadMs() : timeout.getReadMs());

    return merged;
  }

  public static class ClientProperties {

    /**
     * Optional per-client timeout override.
     */
    private TimeoutProperties timeout;

      @SuppressFBWarnings(
              value = "EI_EXPOSE_REP",
              justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
      )
    public TimeoutProperties getTimeout() {
      return timeout;
    }

      @SuppressFBWarnings(
              value = "EI_EXPOSE_REP2",
              justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
      )
    public void setTimeout(TimeoutProperties timeout) {
          this.timeout = timeout == null ? new TimeoutProperties() : timeout;
    }
  }

  /**
   * Timeout configuration in milliseconds.
   */
  public static class TimeoutProperties {

    /**
     * Connect timeout in milliseconds.
     * <p>
     * Default: 1000 ms.
     */
    private Integer connectMs = 1000;

    /**
     * Read/response timeout in milliseconds.
     * <p>
     * Default: 5000 ms.
     */
    private Integer readMs = 5000;

    public Integer getConnectMs() {
      return connectMs;
    }

    public void setConnectMs(Integer connectMs) {
      this.connectMs = connectMs;
    }

    public Integer getReadMs() {
      return readMs;
    }

    public void setReadMs(Integer readMs) {
      this.readMs = readMs;
    }
  }
}
