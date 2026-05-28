package io.github.mohankandar.idp.data.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IDP cache configuration.
 *
 * <p>Caching is intentionally <b>opt-in</b> via {@code idp.cache.enabled=true}.
 */
@ConfigurationProperties(prefix = "idp.cache")
public class IdpCacheProperties {

  /** Master switch for caching. Default false (opt-in). */
  private boolean enabled = false;

  /**
   * If true and Redis is not reachable during startup, fail fast (do not start the service).
   * Default false: fall back to NoOp cache so the service remains available.
   */
  private boolean failFast = false;

  /**
   * If true, cache operation errors (GET/PUT/EVICT/CLEAR) are swallowed so the app continues
   * and falls back to the underlying method execution (typically the database).
   *
   * <p>Default true: cache is treated as an optimization, not a hard dependency.
   */
  private boolean failOpen = true;

  /** Default TTL applied to caches that do not specify an override. */
  private Duration defaultTtl = Duration.ofMinutes(10);

  /** Per-cache TTL overrides, e.g. idp.cache.ttl.sites=2m */
  private Map<String, Duration> ttl = new LinkedHashMap<>();

  /** Optional prefix for Redis cache keys (useful when sharing a Redis cluster). */
  private String keyPrefix;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isFailFast() {
    return failFast;
  }

  public void setFailFast(boolean failFast) {
    this.failFast = failFast;
  }

  public boolean isFailOpen() {
    return failOpen;
  }

  public void setFailOpen(boolean failOpen) {
    this.failOpen = failOpen;
  }

  public Duration getDefaultTtl() {
    return defaultTtl;
  }

  public void setDefaultTtl(Duration defaultTtl) {
    this.defaultTtl = defaultTtl;
  }

  /**
   * Returns an unmodifiable view to prevent external mutation of internal state.
   */
  public Map<String, Duration> getTtl() {
    return ttl == null ? Map.of() : Collections.unmodifiableMap(ttl);
  }

  /**
   * Defensive copy to prevent external references from mutating internal state.
   */
  public void setTtl(Map<String, Duration> ttl) {
    this.ttl = (ttl == null) ? new LinkedHashMap<>() : new LinkedHashMap<>(ttl);
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public void setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
  }
}