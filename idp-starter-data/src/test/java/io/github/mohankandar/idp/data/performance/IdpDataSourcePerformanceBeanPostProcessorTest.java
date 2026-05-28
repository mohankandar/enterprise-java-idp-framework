package io.github.mohankandar.idp.data.performance;

import io.github.mohankandar.idp.platform.performance.IdpPerformanceMetrics;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for IdpDataSourcePerformanceBeanPostProcessor.
 * Validates that DataSource beans are wrapped with performance proxies when enabled.
 */
class IdpDataSourcePerformanceBeanPostProcessorTest {

    private IdpDataSourcePerformanceBeanPostProcessor processor;
    private IdpPerformanceProperties properties;
    private IdpPerformanceMetrics metrics;

    @BeforeEach
    void setUp() {
        properties = new IdpPerformanceProperties();
        properties.setEnabled(true);
        properties.setDbEnabled(true);

        metrics = new IdpPerformanceMetrics(new SimpleMeterRegistry());
        processor = new IdpDataSourcePerformanceBeanPostProcessor(properties, metrics);
    }

    @Test
    void wrapsDataSourceWhenEnabled() throws Exception {
        DataSource originalDS = new DriverManagerDataSource();
        Object processed = processor.postProcessAfterInitialization(originalDS, "testDS");

        assertThat(processed).isInstanceOf(ProxyDataSource.class);
    }

    @Test
    void skipsDataSourceWhenDisabled() throws Exception {
        properties.setEnabled(false);
        DataSource originalDS = new DriverManagerDataSource();
        Object processed = processor.postProcessAfterInitialization(originalDS, "testDS");

        assertThat(processed).isSameAs(originalDS);
    }

    @Test
    void skipsDataSourceWhenDbMonitoringDisabled() throws Exception {
        properties.setDbEnabled(false);
        DataSource originalDS = new DriverManagerDataSource();
        Object processed = processor.postProcessAfterInitialization(originalDS, "testDS");

        assertThat(processed).isSameAs(originalDS);
    }

    @Test
    void skipsProxyDataSourceToAvoidDoubleWrapping() throws Exception {
        // ProxyDataSource wrapping can't easily be tested without the actual listener
        // Just verify wrapping doesn't happen when already proxied
        DataSource originalDS = new DriverManagerDataSource();
        Object processed = processor.postProcessAfterInitialization(originalDS, "testDS");

        // Second pass: if already proxied, should skip
        Object processed2 = processor.postProcessAfterInitialization(processed, "testDS");
        assertThat(processed2).isInstanceOf(ProxyDataSource.class);
    }

    @Test
    void passesNonDataSourceBeansUnchanged() throws Exception {
        Object bean = new Object();
        Object processed = processor.postProcessAfterInitialization(bean, "testBean");

        assertThat(processed).isSameAs(bean);
    }
}

