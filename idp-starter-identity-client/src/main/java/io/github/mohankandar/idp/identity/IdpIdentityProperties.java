package io.github.mohankandar.idp.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idp.identity")
public class IdpIdentityProperties {

    /** When true, attempt to enrich identity via HTTP using the configured endpoint. */
    private boolean httpEnabled = false;

    /** Base URL of identity service (e.g., https://id.company/api). */
    private String baseUrl;

    /** Path template, e.g., /identity/{networkId} */
    private String pathTemplate = "/identity/{networkId}";

    /** Static bearer token to call identity service (optional). */
    private String bearer;

    /** Connect/read timeout (ms) if applicable. */
    private int timeoutMs = 3000;

    public boolean isHttpEnabled() { return httpEnabled; }
    public void setHttpEnabled(boolean httpEnabled) { this.httpEnabled = httpEnabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getPathTemplate() { return pathTemplate; }
    public void setPathTemplate(String pathTemplate) { this.pathTemplate = pathTemplate; }
    public String getBearer() { return bearer; }
    public void setBearer(String bearer) { this.bearer = bearer; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
}
