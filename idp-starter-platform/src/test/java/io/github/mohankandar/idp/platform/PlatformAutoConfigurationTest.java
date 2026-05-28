package io.github.mohankandar.idp.platform;

import io.github.mohankandar.idp.test.annotations.IdpIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for IDP platform auto-configuration.
 * Validates that platform beans are wired correctly when starters are active.
 */
@IdpIntegrationTest
class PlatformAutoConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void platformAutoConfigurationLoads() {
        assertThat(context).isNotNull();
        assertThat(context.containsBean("platformAutoConfiguration")).isTrue();
    }

    @Test
    void globalExceptionHandlerIsRegistered() {
        assertThat(context.containsBean("idpWebErrorAutoConfiguration")).isTrue();
    }

    @Test
    void openApiSecurityAutoConfigIsRegistered() {
        assertThat(context.containsBean("idpOpenApiSecurityAutoConfiguration")).isTrue();
    }

    @Test
    void restTemplateConfigIsRegistered() {
        assertThat(context.containsBean("restTemplateConfig")).isTrue();
    }

    @Test
    void webClientConfigIsRegistered() {
        assertThat(context.containsBean("webClientConfig")).isTrue();
    }

    @Test
    void performanceAutoConfigIsRegistered() {
        assertThat(context.containsBean("idpPerformanceAutoConfiguration")).isTrue();
    }
}

