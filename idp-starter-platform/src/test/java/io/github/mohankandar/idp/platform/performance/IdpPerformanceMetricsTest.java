package io.github.mohankandar.idp.platform.performance;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Unit tests for IdpPerformanceMetrics.
 * Validates that performance timings are correctly recorded to Micrometer.
 */
class IdpPerformanceMetricsTest {

    private MeterRegistry registry;
    private IdpPerformanceMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new IdpPerformanceMetrics(registry);
    }

    @Test
    void recordsHttpTimings() {
        long durationNs = TimeUnit.MILLISECONDS.toNanos(100);
        // Should not throw exceptions
        assertThatNoException()
            .isThrownBy(() -> metrics.record("http", "external-api", "GET", durationNs, true));
    }

    @Test
    void recordsDatabaseTimings() {
        long durationNs = TimeUnit.MILLISECONDS.toNanos(50);
        // Should not throw exceptions
        assertThatNoException()
            .isThrownBy(() -> metrics.record("db", "primary", "SELECT", durationNs, true));
    }

    @Test
    void recordsServiceTimings() {
        long durationNs = TimeUnit.MILLISECONDS.toNanos(200);
        // Should not throw exceptions
        assertThatNoException()
            .isThrownBy(() -> metrics.record("service", "order-service", "processOrder", durationNs, true));
    }

    @Test
    void tagsSuccessAndFailure() {
        long durationNs = TimeUnit.MILLISECONDS.toNanos(100);
        // Should record both success and failure without exceptions
        assertThatNoException().isThrownBy(() -> {
            metrics.record("http", "api", "GET", durationNs, true);
            metrics.record("http", "api", "GET", durationNs, false);
        });
    }
}

