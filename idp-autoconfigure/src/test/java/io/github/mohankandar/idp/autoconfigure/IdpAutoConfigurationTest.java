package io.github.mohankandar.idp.autoconfigure;

import io.github.mohankandar.idp.test.annotations.IdpIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for IDP base auto-configuration.
 * Validates that core auto-config beans are registered and accessible.
 */
@IdpIntegrationTest
class IdpAutoConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void idpAutoConfigurationLoads() {
        assertThat(context).isNotNull();
        assertThat(context.containsBean("idpAutoConfiguration")).isTrue();
    }

    @Test
    void configurationPropertiesAreLoaded() {
        assertThat(context.getEnvironment()).isNotNull();
        // Verify that framework properties are accessible
        assertThat(context.getEnvironment().getProperty("spring.application.name"))
            .isNotEmpty();
    }

    @Test
    void webApplicationContextIsInitialized() {
        assertThat(context.getDisplayName()).isNotEmpty();
    }
}

