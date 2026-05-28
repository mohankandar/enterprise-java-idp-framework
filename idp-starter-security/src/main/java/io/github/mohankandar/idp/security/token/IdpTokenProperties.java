package io.github.mohankandar.idp.security.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "idp.security.token")
public class IdpTokenProperties {

    private Map<String, Object> claimsDefaults = new LinkedHashMap<>();
    private List<String> claimsAllowlist = new ArrayList<>();

    public Map<String, Object> getClaimsDefaults() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(claimsDefaults));
    }

    public void setClaimsDefaults(Map<String, Object> claimsDefaults) {
        this.claimsDefaults = claimsDefaults == null ? new LinkedHashMap<>() : new LinkedHashMap<>(claimsDefaults);
    }

    public List<String> getClaimsAllowlist() {
        return Collections.unmodifiableList(new ArrayList<>(claimsAllowlist));
    }

    public void setClaimsAllowlist(List<String> claimsAllowlist) {
        this.claimsAllowlist = claimsAllowlist == null ? new ArrayList<>() : new ArrayList<>(claimsAllowlist);
    }
}
