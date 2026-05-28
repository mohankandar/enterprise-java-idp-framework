package io.github.mohankandar.idp.partner.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unified Partner Services configuration for IDP.
 *
 * <p>Root: {@code idp.partners.services.<partnerName>}.</p>
 */
@ConfigurationProperties(prefix = "idp.partners")
public class IdpPartnerProperties {

    /**
     * Partner service configurations keyed by partner name.
     */
    private Map<String, PartnerServiceProperties> services = new HashMap<>();

    public Map<String, PartnerServiceProperties> getServices() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(services));
    }

    public void setServices(Map<String, PartnerServiceProperties> services) {
        this.services = (services == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(services);
    }

    public enum PartnerType { REST, SOAP }

    public enum RestAuthMode { NONE, PROPAGATE_BEARER, API_KEY, OAUTH }

    public static class PartnerServiceProperties {

        private PartnerType type;
        private RestProperties rest;
        private SoapProperties soap;

        public PartnerType getType() {
            return type;
        }

        public void setType(PartnerType type) {
            this.type = type;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public RestProperties getRest() {
            return rest;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setRest(RestProperties rest) {
            this.rest = rest;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public SoapProperties getSoap() {
            return soap;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setSoap(SoapProperties soap) {
            this.soap = soap;
        }
    }

    public static class RestProperties {

        private String baseUrl;
        private Integer connectTimeoutMs = 1000;
        private Integer readTimeoutMs = 5000;
        private RestAuthProperties auth = new RestAuthProperties();
        private Map<String, String> headers = new HashMap<>();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Integer getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(Integer connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public Integer getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(Integer readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public RestAuthProperties getAuth() {
            return auth;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setAuth(RestAuthProperties auth) {
            this.auth = auth;
        }

        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = (headers == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
        }
    }

    public static class RestAuthProperties {
        private RestAuthMode mode = RestAuthMode.PROPAGATE_BEARER;
        private String apiKeyHeader = "X-API-Key";
        private String apiKeyValue;
        /**
         * Canonical property path: rest.auth.oauthClientCredentials.*
         */
        private OAuthClientCredentialsProperties oauthClientCredentials = new OAuthClientCredentialsProperties();
        /**
         * Backward-compatible alias for older apps that used rest.auth.oauth.*
         */
        private OAuthClientCredentialsProperties oauth = new OAuthClientCredentialsProperties();

        public RestAuthMode getMode() {
            return mode;
        }

        public void setMode(RestAuthMode mode) {
            this.mode = mode;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKeyValue() {
            return apiKeyValue;
        }

        public void setApiKeyValue(String apiKeyValue) {
            this.apiKeyValue = apiKeyValue;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public OAuthClientCredentialsProperties getOauthClientCredentials() {
            return oauthClientCredentials;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setOauthClientCredentials(OAuthClientCredentialsProperties oauthClientCredentials) {
            this.oauthClientCredentials = oauthClientCredentials;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public OAuthClientCredentialsProperties getOauth() {
            return oauth;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setOauth(OAuthClientCredentialsProperties oauth) {
            this.oauth = oauth;
        }

        /**
         * Resolve OAuth client-credentials settings from the canonical subtree first,
         * then fall back to the legacy alias subtree when older applications still use it.
         */
        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally returns nested mutable property bean for runtime resolution."
        )
        public OAuthClientCredentialsProperties resolveOauthClientCredentials() {
            if (oauthClientCredentials != null && oauthClientCredentials.hasAnyConfiguredValue()) {
                return oauthClientCredentials;
            }
            return oauth != null ? oauth : oauthClientCredentials;
        }

    }

    public static class OAuthClientCredentialsProperties {
        private String tokenUrl;
        /**
         * Backward-compatible alias for apps that used accessTokenUrl instead of tokenUrl.
         */
        private String accessTokenUrl;
        private String clientId;
        private String clientSecret;
        private String scope;
        private String audience;

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getAccessTokenUrl() {
            return accessTokenUrl;
        }

        public void setAccessTokenUrl(String accessTokenUrl) {
            this.accessTokenUrl = accessTokenUrl;
        }

        /**
         * Canonical getter used by framework code and validators.
         */
        public String resolveTokenUrl() {
            return tokenUrl != null && !tokenUrl.isBlank() ? tokenUrl : accessTokenUrl;
        }

        public boolean hasAnyConfiguredValue() {
            return (tokenUrl != null && !tokenUrl.isBlank())
                || (accessTokenUrl != null && !accessTokenUrl.isBlank())
                || (clientId != null && !clientId.isBlank())
                || (clientSecret != null && !clientSecret.isBlank())
                || (scope != null && !scope.isBlank())
                || (audience != null && !audience.isBlank());
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }
    }

    public static class SoapProperties {

        private String endpoint;
        private Integer connectTimeoutMs = 3000;
        private Integer readTimeoutMs = 15000;
        private ProxyProperties proxy = new ProxyProperties();
        private TlsProperties tls = new TlsProperties();
        private Map<String, String> headers = new HashMap<>();

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Integer getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(Integer connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public Integer getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(Integer readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public ProxyProperties getProxy() {
            return proxy;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setProxy(ProxyProperties proxy) {
            this.proxy = proxy;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP",
                justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
        )
        public TlsProperties getTls() {
            return tls;
        }

        @SuppressFBWarnings(
                value = "EI_EXPOSE_REP2",
                justification = "Spring @ConfigurationProperties holder intentionally stores nested mutable property bean for binding."
        )
        public void setTls(TlsProperties tls) {
            this.tls = tls;
        }

        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = (headers == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
        }
    }

    public static class ProxyProperties {
        private boolean enabled;
        private String host;
        private int port;
        private String username;
        private String password;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class TlsProperties {
        private String truststore;
        private String truststorePassword;
        private String truststoreType = "JKS";
        private boolean disableCnCheck;

        public String getTruststore() {
            return truststore;
        }

        public void setTruststore(String truststore) {
            this.truststore = truststore;
        }

        public String getTruststorePassword() {
            return truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        public String getTruststoreType() {
            return truststoreType;
        }

        public void setTruststoreType(String truststoreType) {
            this.truststoreType = truststoreType;
        }

        public boolean isDisableCnCheck() {
            return disableCnCheck;
        }

        public void setDisableCnCheck(boolean disableCnCheck) {
            this.disableCnCheck = disableCnCheck;
        }
    }
}
