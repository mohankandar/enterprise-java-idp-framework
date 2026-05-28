package io.github.mohankandar.idp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.security.config.IdpWebSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAutoConfigurationContextRunnerTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    SecurityAutoConfiguration.class
            ))
            .withUserConfiguration(TestSupportConfig.class);

    @Test
    void securityEnabledByDefaultRegistersPrimarySecurityBeans() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(SecurityProperties.class);
            assertThat(context).hasSingleBean(IdpWebSecurityProperties.class);
            assertThat(context).hasBean("idpSecurityFilterChain");
            assertThat(context).hasBean("idpAuthenticationEntryPoint");
            assertThat(context).hasBean("idpAccessDeniedHandler");
            assertThat(context).hasBean("idpBearerTokenResolver");
        });
    }

    @Test
    void disabledSecuritySwitchesToPermitAllChain() {
        runner.withPropertyValues("idp.security.enabled=false")
                .run(context -> {
                    assertThat(context).hasBean("idpSecurityDisabledFilterChain");
                    assertThat(context).doesNotHaveBean("idpSecurityFilterChain");
                });
    }

    @Test
    void apiKeyFilterAppearsOnlyWhenEnabled() {
        runner.withPropertyValues(
                        "idp.security.api-key.enabled=true",
                        "idp.security.api-key.value=test-key"
                )
                .run(context -> assertThat(context).hasSingleBean(ApiKeyAuthFilter.class));

        runner.run(context -> assertThat(context).doesNotHaveBean(ApiKeyAuthFilter.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestSupportConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean(name = "mvcHandlerMappingIntrospector")
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }
}
