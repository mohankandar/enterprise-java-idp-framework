package io.github.mohankandar.idp.demo.debug;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Logs cache/redis wiring at startup.
 *
 * <p>This is intentionally verbose for demo-service so we can quickly verify whether
 * IDP caching auto-config is actually using RedisCacheManager vs a fallback.
 */
@Component
public class CacheStartupLogger implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(CacheStartupLogger.class);

  private final ApplicationContext ctx;
  private final Environment env;

  public CacheStartupLogger(ApplicationContext ctx, Environment env) {
    this.ctx = ctx;
    this.env = env;
  }

  @Override
  public void run(ApplicationArguments args) {
    log.info("=== IDP Cache Startup Status ===");
    log.info("activeProfiles={}", (Object) env.getActiveProfiles());
    log.info("idp.cache.enabled={}", env.getProperty("idp.cache.enabled"));
    log.info("spring.data.redis.host={}", env.getProperty("spring.data.redis.host"));
    log.info("spring.data.redis.port={}", env.getProperty("spring.data.redis.port"));
    log.info("idp.data.redis.embedded.enabled={}", env.getProperty("idp.data.redis.embedded.enabled"));

    Map<String, CacheManager> cms = ctx.getBeansOfType(CacheManager.class);
    log.info("cacheManagerBeanCount={}, beans={}", cms.size(), cms.keySet());
    cms.forEach((name, cm) -> log.info("cacheManagerBean='{}', class='{}', cacheNames={}",
        name,
        cm.getClass().getName(),
        cm.getCacheNames()));

    Map<String, RedisConnectionFactory> rcfs = ctx.getBeansOfType(RedisConnectionFactory.class);
    log.info("redisConnectionFactoryBeanCount={}, beans={}", rcfs.size(), rcfs.keySet());
    rcfs.forEach((name, rcf) -> log.info("redisConnectionFactoryBean='{}', class='{}'",
        name,
        rcf.getClass().getName()));

    RedisConnectionFactory rcf = rcfs.values().stream().findFirst().orElse(null);
    if (rcf == null) {
      log.warn("RedisConnectionFactory not present in context");
      return;
    }

    try (RedisConnection conn = rcf.getConnection()) {
      String pong = conn.ping();
      log.info("redisPing='{}'", pong);
    } catch (Exception e) {
      log.warn("redisPing failed: {}: {}", e.getClass().getName(), e.getMessage());
    }
  }
}
