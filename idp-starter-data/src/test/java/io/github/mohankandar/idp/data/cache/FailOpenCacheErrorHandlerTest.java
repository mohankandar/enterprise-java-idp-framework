package io.github.mohankandar.idp.data.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for FailOpenCacheErrorHandler.
 * Validates that cache errors are swallowed (fail-open) to keep service available.
 */
class FailOpenCacheErrorHandlerTest {

    private FailOpenCacheErrorHandler handler;
    private Cache mockCache;
    private RuntimeException cacheException;

    @BeforeEach
    void setUp() {
        handler = new FailOpenCacheErrorHandler();
        mockCache = mock(Cache.class);
        when(mockCache.getName()).thenReturn("testCache");
        cacheException = new RuntimeException("Redis connection refused");
    }

    @Test
    void handleCacheGetErrorDoesNotThrow() {
        assertThatNoException()
            .isThrownBy(() -> handler.handleCacheGetError(cacheException, mockCache, "key1"));
    }

    @Test
    void handleCachePutErrorDoesNotThrow() {
        assertThatNoException()
            .isThrownBy(() -> handler.handleCachePutError(cacheException, mockCache, "key1", "value1"));
    }

    @Test
    void handleCacheEvictErrorDoesNotThrow() {
        assertThatNoException()
            .isThrownBy(() -> handler.handleCacheEvictError(cacheException, mockCache, "key1"));
    }

    @Test
    void handleCacheClearErrorDoesNotThrow() {
        assertThatNoException()
            .isThrownBy(() -> handler.handleCacheClearError(cacheException, mockCache));
    }

    @Test
    void handlesNullCacheGracefully() {
        assertThatNoException()
            .isThrownBy(() -> handler.handleCacheGetError(cacheException, null, "key1"));
    }
}

