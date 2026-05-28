package io.github.mohankandar.idp.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idp.data")
public class IdpDataProperties {

    private final RedisEmbedded redisEmbedded = new RedisEmbedded();

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "Spring @ConfigurationProperties holder intentionally exposes nested mutable property bean for binding."
    )
    public RedisEmbedded getRedisEmbedded() {
        return redisEmbedded;
    }
    public static class RedisEmbedded {
        /** Start an embedded Redis (for local/dev only). */
        private boolean enabled = false;
        /** Port to bind embedded Redis to. */
        private int port = 6379;
        /** Auto-start only when active profile contains "local". */
        private boolean onlyWhenLocalProfile = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public boolean isOnlyWhenLocalProfile() { return onlyWhenLocalProfile; }
        public void setOnlyWhenLocalProfile(boolean onlyWhenLocalProfile) { this.onlyWhenLocalProfile = onlyWhenLocalProfile; }
    }
}
