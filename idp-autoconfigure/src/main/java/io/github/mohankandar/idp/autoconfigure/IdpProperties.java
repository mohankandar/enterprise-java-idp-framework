package io.github.mohankandar.idp.autoconfigure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Central properties used across starters.
 * Keep it small in Phase 1; add nested classes as needed later.
 */
@ConfigurationProperties(prefix = "idp")
public class IdpProperties {

    /**
     * Friendly application name shown in logs/metrics if available.
     * Defaults to spring.application.name when present (resolved in platform starter).
     */
    private String appName;

    private final Logging logging = new Logging();
    private final Feature feature = new Feature();

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property beans for binding."
    )
    public Logging getLogging() {
        return logging;
    }

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property beans for binding."
    )
    public Feature getFeature() {
        return feature;
    }

    public static class Logging {
        /**
         * When true, platform filters will mask sensitive values in request/response logs.
         */
        private boolean maskingEnabled = true;

        public boolean isMaskingEnabled() { return maskingEnabled; }
        public void setMaskingEnabled(boolean maskingEnabled) { this.maskingEnabled = maskingEnabled; }
    }

    public static class Feature {
        /**
         * Config-only feature flags (starter-platform can read this map; keep simple boolean root for now).
         * You can expand to a Map<String,Boolean> in Phase 2 if desired.
         */
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
