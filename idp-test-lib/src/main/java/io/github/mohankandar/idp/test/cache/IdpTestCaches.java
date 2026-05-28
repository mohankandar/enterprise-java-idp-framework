package io.github.mohankandar.idp.test.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/** Simple cache utilities for deterministic unit/integration tests. */
public final class IdpTestCaches {

  private IdpTestCaches() {}

  public static CacheManager inMemoryCacheManager(String... cacheNames) {
    if (cacheNames == null || cacheNames.length == 0) {
      return new ConcurrentMapCacheManager();
    }
    return new ConcurrentMapCacheManager(cacheNames);
  }

  public static void clearAll(CacheManager cacheManager) {
    if (cacheManager == null) {
      return;
    }
    for (String name : cacheManager.getCacheNames()) {
      var cache = cacheManager.getCache(name);
      if (cache != null) {
        cache.clear();
      }
    }
  }
}
