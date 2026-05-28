package io.github.mohankandar.idp.data.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Lightweight helpers. Spring Boot already creates the ConnectionFactory from spring.data.redis.*.
 * We just offer sane defaults for templates if the app doesn’t define them.
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        var t = new RedisTemplate<String, Object>();
        t.setConnectionFactory(cf);
        // Keep default JDK serialization for now; apps can override with Jackson/JSON if desired.
        t.afterPropertiesSet();
        return t;
    }
}
