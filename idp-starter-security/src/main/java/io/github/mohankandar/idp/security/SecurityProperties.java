package io.github.mohankandar.idp.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "idp.security")
public class SecurityProperties {

    /** Master switch for all IDP security behavior. */
    private boolean enabled = true;

    /** JWT claim names mapping */
    private final Claims claims = new Claims();

    /** API key mode (optional) */
    private final ApiKey apiKey = new ApiKey();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
    public Claims getClaims() { return claims; }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
    public ApiKey getApiKey() { return apiKey; }

    public static class Claims {
        private String applicationName = "idp.app.name";
        private String appToken = "idp.app.token";
        private String firstName = "idp.user.first_name";
        private String lastName = "idp.user.last_name";
        private String networkId = "idp.user.network_id";
        private String roles = "roles";
        private String scopes = "scope";

        public String getApplicationName() { return applicationName; }
        public void setApplicationName(String s) { this.applicationName = s; }
        public String getAppToken() { return appToken; }
        public void setAppToken(String s) { this.appToken = s; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String s) { this.firstName = s; }
        public String getLastName() { return lastName; }
        public void setLastName(String s) { this.lastName = s; }
        public String getNetworkId() { return networkId; }
        public void setNetworkId(String s) { this.networkId = s; }
        public String getRoles() { return roles; }
        public void setRoles(String s) { this.roles = s; }
        public String getScopes() { return scopes; }
        public void setScopes(String s) { this.scopes = s; }
    }

    public static class ApiKey {
        private boolean enabled = false;
        private String header = "X-API-Key";
        private String value = "";
        private Map<String, Client> clients = new LinkedHashMap<>();
        private String applicationNameHeader = "X-App-Name";
        private String appTokenHeader = "X-App-Token";
        private String firstNameHeader = "X-User-FirstName";
        private String lastNameHeader = "X-User-LastName";
        private String networkIdHeader = "X-User-NetworkId";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public Map<String, Client> getClients() {
            return Collections.unmodifiableMap(new LinkedHashMap<>(clients));
        }

        public void setClients(Map<String, Client> clients) {
            this.clients = clients == null ? new LinkedHashMap<>() : new LinkedHashMap<>(clients);
        }

        public String getApplicationNameHeader() { return applicationNameHeader; }
        public void setApplicationNameHeader(String s) { this.applicationNameHeader = s; }
        public String getAppTokenHeader() { return appTokenHeader; }
        public void setAppTokenHeader(String s) { this.appTokenHeader = s; }
        public String getFirstNameHeader() { return firstNameHeader; }
        public void setFirstNameHeader(String s) { this.firstNameHeader = s; }
        public String getLastNameHeader() { return lastNameHeader; }
        public void setLastNameHeader(String s) { this.lastNameHeader = s; }
        public String getNetworkIdHeader() { return networkIdHeader; }
        public void setNetworkIdHeader(String s) { this.networkIdHeader = s; }

        public static class Client {
            private List<String> keys = new ArrayList<>();

            public List<String> getKeys() {
                return Collections.unmodifiableList(new ArrayList<>(keys));
            }

            public void setKeys(List<String> keys) {
                this.keys = keys == null ? new ArrayList<>() : new ArrayList<>(keys);
            }
        }
    }
}
