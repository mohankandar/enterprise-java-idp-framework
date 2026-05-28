package io.github.mohankandar.idp.data.cache;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Actuator health indicator that reports cache state.
 *
 * <p>Goal: observability without taking the service down if Redis is temporarily unavailable.
 */
public class IdpCacheHealthIndicator implements HealthIndicator {

  private final CacheManager cacheManager;
  private final RedisConnectionFactory redisConnectionFactory;

  public IdpCacheHealthIndicator(CacheManager cacheManager, RedisConnectionFactory redisConnectionFactory) {
    this.cacheManager = cacheManager;
    this.redisConnectionFactory = redisConnectionFactory;
  }

  @Override
  public Health health() {
    boolean redisUp = ping(redisConnectionFactory);
    String cm = (cacheManager == null) ? "none" : cacheManager.getClass().getSimpleName();

    // Always UP: cache is an optimization. If Redis is down, IDP can still serve requests (NoOp cache fallback).
    return Health.up()
        .withDetail("cacheManager", cm)
        .withDetail("redisReachable", redisUp)
        .build();
  }

  private boolean ping(RedisConnectionFactory cf) {
    if (cf == null) return false;
    try (RedisConnection c = cf.getConnection()) {
      String pong = c.ping();
      return pong != null && !pong.isBlank();
    } catch (Exception e) {
      return false;
    }
  }
}
