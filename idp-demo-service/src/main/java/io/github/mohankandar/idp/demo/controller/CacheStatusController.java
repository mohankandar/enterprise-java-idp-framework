package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.demo.service.DemoCacheService;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo/cache")
public class CacheStatusController {

  private final ApplicationContext ctx;
  private final Environment env;

  private final DemoCacheService service;
  public CacheStatusController(ApplicationContext ctx, Environment env, DemoCacheService service) {
    this.ctx = ctx;
    this.env = env;
    this.service =service;
  }

  @GetMapping("/status")
  public Map<String, Object> status() {
    Map<String, Object> out = new LinkedHashMap<>();

    // --- Profiles / config sanity
    out.put("activeProfiles", env.getActiveProfiles());
    out.put("spring.data.redis.host", env.getProperty("spring.data.redis.host"));
    out.put("spring.data.redis.port", env.getProperty("spring.data.redis.port"));
    out.put("idp.cache.enabled", env.getProperty("idp.cache.enabled"));
    out.put("idp.data.redis.embedded.enabled", env.getProperty("idp.data.redis.embedded.enabled"));

    // --- Cache manager
    Map<String, CacheManager> cms = ctx.getBeansOfType(CacheManager.class);
    out.put("cacheManagerBeanCount", cms.size());
    out.put("cacheManagerBeans", cms.keySet());

    CacheManager cm = cms.values().stream().findFirst().orElse(null);
    out.put("cacheManagerClass", cm == null ? null : cm.getClass().getName());
    out.put("cacheNames", cm == null ? null : cm.getCacheNames());

    // --- Redis connection factory + ping test
    Map<String, RedisConnectionFactory> rcfs = ctx.getBeansOfType(RedisConnectionFactory.class);
    out.put("redisConnectionFactoryBeanCount", rcfs.size());
    out.put("redisConnectionFactoryBeans", rcfs.keySet());

    RedisConnectionFactory rcf = rcfs.values().stream().findFirst().orElse(null);
    out.put("redisConnectionFactoryClass", rcf == null ? null : rcf.getClass().getName());

    if (rcf != null) {
      Map<String, Object> ping = new LinkedHashMap<>();
      try (RedisConnection conn = rcf.getConnection()) {
        // ping() returns byte[] in most drivers
        String resp = conn.ping();
        ping.put("ok", true);
        ping.put("response", resp);
      } catch (Exception e) {
        ping.put("ok", false);
        ping.put("errorClass", e.getClass().getName());
        ping.put("message", e.getMessage());
      }
      out.put("redisPing", ping);
    } else {
      out.put("redisPing", null);
    }

    return out;
  }

  @GetMapping("/proof")
  public String proof(@RequestParam("arg0") String k) {

    long start = System.currentTimeMillis();

    String result = service.slow(k);

    long took = System.currentTimeMillis() - start;

    return result + " | tookMs=" + took;
  }
}