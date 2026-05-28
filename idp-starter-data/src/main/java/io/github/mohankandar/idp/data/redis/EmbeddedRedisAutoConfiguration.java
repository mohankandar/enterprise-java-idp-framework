package io.github.mohankandar.idp.data.redis;

import io.github.mohankandar.idp.data.IdpDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Starts an embedded Redis server when: - idp.data.redis.embedded.enabled=true - embedded-redis lib
 * is on the classpath - (optional) only-when-local-profile=true AND "local" profile is active
 */
@AutoConfiguration
@ConditionalOnClass(name = "redis.embedded.RedisServer")
@ConditionalOnProperty(prefix = "idp.data.redis.embedded", name = "enabled", havingValue = "true")
public class EmbeddedRedisAutoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(EmbeddedRedisAutoConfiguration.class);

  @Bean
  @ConditionalOnMissingBean(name = "embeddedRedisLifecycle")
  public SmartLifecycle embeddedRedisLifecycle(IdpDataProperties props, Environment env) {
    return new SmartLifecycle() {
      private volatile boolean running = false;
      private redis.embedded.RedisServer server;

      @Override
      public void start() {
          if (running) {
              return;
          }

        // Honor "only-when-local-profile"
        if (props.getRedisEmbedded().isOnlyWhenLocalProfile()
            && !env.acceptsProfiles(Profiles.of("local"))) {
          log.info(
              "Embedded Redis: skipped (only-when-local-profile=true and 'local' not active).");
          return;
        }

        int port = props.getRedisEmbedded().getPort();
        try {
          // If you plan to support port=0 (random), implement a free-port finder here.
          server = new redis.embedded.RedisServer(port);
          server.start();
          running = true;
          log.info("Embedded Redis: started on port {}", port);
        } catch (Exception e) {
          log.warn("Embedded Redis failed to start on port {}: {}", port, e.toString());
        }
      }

      @Override
      public void stop() {
        if (server != null) {
          try {
            server.stop();
            log.info("Embedded Redis: stopped.");
          } catch (Exception e) {
            log.debug("Embedded Redis stop suppressed: {}", e.toString());
          } finally {
            server = null;
          }
        }
        running = false;
      }

      @Override
      public boolean isRunning() {
        return running;
      }

      @Override
      public int getPhase() {
        return Integer.MIN_VALUE;
      } // start early

      @Override
      public boolean isAutoStartup() {
        return true;
      }

      @Override
      public void stop(Runnable callback) {
        stop();
        callback.run();
      }
    };
  }
}
