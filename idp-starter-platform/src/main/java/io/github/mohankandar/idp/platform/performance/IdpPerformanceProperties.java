package io.github.mohankandar.idp.platform.performance;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idp.perf")
public class IdpPerformanceProperties {

    private boolean enabled = true;
    private boolean dbEnabled = true;
    private final Thresholds thresholds = new Thresholds();
    private final Metrics metrics = new Metrics();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDbEnabled() {
        return dbEnabled;
    }

    public void setDbEnabled(boolean dbEnabled) {
        this.dbEnabled = dbEnabled;
    }

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
    public Thresholds getThresholds() {
        return thresholds;
    }

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
    public Metrics getMetrics() {
        return metrics;
    }

    public static class Thresholds {
        private long controllerMs = 300;
        private long serviceMs = 150;
        private long httpMs = 400;
        private long dbMs = 200;

        public long getControllerMs() {
            return controllerMs;
        }

        public void setControllerMs(long controllerMs) {
            this.controllerMs = controllerMs;
        }

        public long getServiceMs() {
            return serviceMs;
        }

        public void setServiceMs(long serviceMs) {
            this.serviceMs = serviceMs;
        }

        public long getHttpMs() {
            return httpMs;
        }

        public void setHttpMs(long httpMs) {
            this.httpMs = httpMs;
        }

        public long getDbMs() {
            return dbMs;
        }

        public void setDbMs(long dbMs) {
            this.dbMs = dbMs;
        }
    }

    public static class Metrics {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
