package io.github.mohankandar.idp.data.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * IDP cache auto-configuration.
 *
 * <p>Design goals:
 * <ul>
 *   <li>Opt-in via {@code idp.cache.enabled=true}</li>
 *   <li>Production-safe: Redis down should not take the service down (unless fail-fast is enabled)</li>
 *   <li>Supports embedded Redis in local (provided by existing IDP embedded Redis auto-config)</li>
 * </ul>
 */
// Ensure RedisConnectionFactory exists (after RedisAutoConfiguration)
// but register our CacheManager before Boot's CacheAutoConfiguration so Boot doesn't create its own.
@AutoConfiguration(after = RedisAutoConfiguration.class, before = CacheAutoConfiguration.class)
@EnableCaching
@EnableConfigurationProperties(IdpCacheProperties.class)
@ConditionalOnProperty(prefix = "idp.cache", name = "enabled", havingValue = "true")
@ConditionalOnClass(CacheManager.class)
public class IdpCacheAutoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(IdpCacheAutoConfiguration.class);

  @Bean(name = "cacheManager")
  @Primary
  @ConditionalOnBean(RedisConnectionFactory.class)
  @ConditionalOnMissingBean(CacheManager.class)
  public CacheManager idpCacheManager(IdpCacheProperties props, RedisConnectionFactory connectionFactory) {

    boolean reachable = true;
    if (props.isFailFast()) {
      reachable = isRedisReachable(connectionFactory);
    }
    if (!reachable) {
      String msg = "Redis not reachable at startup.";
      if (props.isFailFast()) {
        log.error("{} fail-fast=true, aborting startup.", msg);
        throw new IllegalStateException("Redis not reachable and idp.cache.fail-fast=true");
      }
      log.warn("{} fail-fast=false, continuing with RedisCacheManager. Caching may error until Redis is available.", msg);
    }

    RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(safeTtl(props.getDefaultTtl()));

    if (hasText(props.getKeyPrefix())) {
      String prefix = props.getKeyPrefix().trim();
      defaults = defaults.computePrefixWith(cacheName -> prefix + ":" + cacheName + "::");
    }

    Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
    if (props.getTtl() != null) {
      for (Map.Entry<String, Duration> e : props.getTtl().entrySet()) {
        String cacheName = e.getKey();
        if (!hasText(cacheName)) continue;
        Duration ttl = safeTtl(e.getValue());
        perCache.put(cacheName.trim(), defaults.entryTtl(ttl));
      }
    }

    log.info("Redis cache enabled (startupReachable={}, startupCheck={}, defaultTTL={}, overrides={})",
        reachable, props.isFailFast(), safeTtl(props.getDefaultTtl()), perCache.keySet());

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaults)
        .withInitialCacheConfigurations(perCache)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean({ CacheManager.class, RedisConnectionFactory.class })
  public CacheManager idpNoOpCacheManager() {
    log.warn("RedisConnectionFactory not found; using NoOpCacheManager (caching effectively disabled)");
    return new NoOpCacheManager();
  }

  /**
   * Fail-open cache error handler.
   *
   * <p>When enabled (default), cache failures will be logged and swallowed so requests
   * continue and fall back to the underlying method execution.
   */
  @Bean
  @ConditionalOnProperty(prefix = "idp.cache", name = "fail-open", havingValue = "true", matchIfMissing = true)
  @ConditionalOnMissingBean(CacheErrorHandler.class)
  public CacheErrorHandler idpCacheErrorHandler() {
    return new FailOpenCacheErrorHandler();
  }

  private boolean isRedisReachable(RedisConnectionFactory cf) {
    try (RedisConnection c = cf.getConnection()) {
      String pong = c.ping();
      return pong != null && !pong.isBlank();
    } catch (Exception e) {
      return false;
    }
  }

  private static Duration safeTtl(Duration ttl) {
    return (ttl == null || ttl.isNegative() || ttl.isZero()) ? Duration.ofMinutes(10) : ttl;
  }

  private static boolean hasText(String s) {
    return s != null && !s.trim().isEmpty();
  }
}
