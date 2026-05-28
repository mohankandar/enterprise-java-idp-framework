package io.github.mohankandar.idp.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * URL-level web security settings. Apps can add to 'permitPaths' in application.yml.
 *
 * Example:
 * idp.security.web.permit-paths:
 *   - /public/**
 *   - /files/download/**
 */
@ConfigurationProperties(prefix = "idp.security.web")
public class IdpWebSecurityProperties {

    /**
     * Additional application-specific paths to permit without auth,
     * merged with IdpDefaultSecurityPaths.DEFAULT_PERMIT_ALL.
     */
    private List<String> permitPaths = new ArrayList<>();

    public List<String> getPermitPaths() {
        return Collections.unmodifiableList(new ArrayList<>(permitPaths));
    }

    public void setPermitPaths(List<String> permitPaths) {
        this.permitPaths = permitPaths == null ? new ArrayList<>() : new ArrayList<>(permitPaths);
    }
}
