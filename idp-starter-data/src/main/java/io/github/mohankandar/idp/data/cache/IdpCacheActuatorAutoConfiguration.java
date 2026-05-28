package io.github.mohankandar.idp.data.cache;

import io.github.mohankandar.idp.data.redis.RedisAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Registers the cache health indicator when actuator is present.
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnProperty(prefix = "idp.cache", name = "enabled", havingValue = "true")
@ConditionalOnBean({CacheManager.class, RedisConnectionFactory.class})
public class IdpCacheActuatorAutoConfiguration {

  @Bean(name = "idpCache")
  public HealthIndicator idpCacheHealthIndicator(CacheManager cacheManager,
      RedisConnectionFactory redisConnectionFactory) {
    return new IdpCacheHealthIndicator(cacheManager, redisConnectionFactory);
  }
}
