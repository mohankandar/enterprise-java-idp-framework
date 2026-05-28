package io.github.mohankandar.idp.platform.performance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for IdpPerformanceProperties - verifies defaults and mutation.
 */
class IdpPerformancePropertiesTest {

    @Test
    void defaultsAreEnabled() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.isDbEnabled()).isTrue();
        assertThat(props.getMetrics().isEnabled()).isTrue();
    }

    @Test
    void canDisableGlobalPerformanceTracking() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        props.setEnabled(false);
        assertThat(props.isEnabled()).isFalse();
    }

    @Test
    void canDisableDbPerformanceTracking() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        props.setDbEnabled(false);
        assertThat(props.isDbEnabled()).isFalse();
    }

    @Test
    void defaultThresholds() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        assertThat(props.getThresholds().getControllerMs()).isEqualTo(300);
        assertThat(props.getThresholds().getServiceMs()).isEqualTo(150);
        assertThat(props.getThresholds().getHttpMs()).isEqualTo(400);
        assertThat(props.getThresholds().getDbMs()).isEqualTo(200);
    }

    @Test
    void canOverrideThresholds() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        props.getThresholds().setControllerMs(500);
        props.getThresholds().setDbMs(100);
        assertThat(props.getThresholds().getControllerMs()).isEqualTo(500);
        assertThat(props.getThresholds().getDbMs()).isEqualTo(100);
    }

    @Test
    void canDisableMetrics() {
        IdpPerformanceProperties props = new IdpPerformanceProperties();
        props.getMetrics().setEnabled(false);
        assertThat(props.getMetrics().isEnabled()).isFalse();
    }
}

