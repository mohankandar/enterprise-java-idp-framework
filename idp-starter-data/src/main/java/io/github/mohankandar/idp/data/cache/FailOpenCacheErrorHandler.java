package io.github.mohankandar.idp.data.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Fail-open cache error handler.
 *
 * <p>Swallows cache operation failures so the underlying method execution can proceed
 * (e.g., database query), keeping the service available when Redis is temporarily unavailable.
 */
public class FailOpenCacheErrorHandler implements CacheErrorHandler {

  private static final Logger log = LoggerFactory.getLogger(FailOpenCacheErrorHandler.class);

  @Override
  public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
    log.warn("Cache GET failed (fail-open). cache={}, key={}, cause={}",
        cacheName(cache), key, exception.toString());
  }

  @Override
  public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
    log.warn("Cache PUT failed (fail-open). cache={}, key={}, cause={}",
        cacheName(cache), key, exception.toString());
  }

  @Override
  public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
    log.warn("Cache EVICT failed (fail-open). cache={}, key={}, cause={}",
        cacheName(cache), key, exception.toString());
  }

  @Override
  public void handleCacheClearError(RuntimeException exception, Cache cache) {
    log.warn("Cache CLEAR failed (fail-open). cache={}, cause={}",
        cacheName(cache), exception.toString());
  }

  private static String cacheName(Cache cache) {
    return cache != null ? cache.getName() : "null";
  }
}
