package io.github.mohankandar.idp.security.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "idp.security.token-endpoint")
public class TokenEndpointProperties {

  private boolean enabled = false;
  private String issuer = "idp-local";
  private long expirySeconds = 900;
  private String hmacSecret = "change-me-change-me-change-me-32b";
  private String kid;
  private boolean restrictByNetwork = false;
  private List<String> allowedCidrs = new ArrayList<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public long getExpirySeconds() {
    return expirySeconds;
  }

  public void setExpirySeconds(long expirySeconds) {
    this.expirySeconds = expirySeconds;
  }

  public String getHmacSecret() {
    return hmacSecret;
  }

  public void setHmacSecret(String hmacSecret) {
    this.hmacSecret = hmacSecret;
  }

  public String getKid() {
    return kid;
  }

  public void setKid(String kid) {
    this.kid = kid;
  }

  public boolean isRestrictByNetwork() {
    return restrictByNetwork;
  }

  public void setRestrictByNetwork(boolean restrictByNetwork) {
    this.restrictByNetwork = restrictByNetwork;
  }

  public List<String> getAllowedCidrs() {
    return Collections.unmodifiableList(new ArrayList<>(allowedCidrs));
  }

  public void setAllowedCidrs(List<String> allowedCidrs) {
    this.allowedCidrs = allowedCidrs == null ? new ArrayList<>() : new ArrayList<>(allowedCidrs);
  }
}
