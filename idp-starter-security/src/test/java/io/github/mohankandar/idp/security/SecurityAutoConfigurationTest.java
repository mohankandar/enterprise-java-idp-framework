package io.github.mohankandar.idp.security;

import io.github.mohankandar.idp.test.annotations.IdpIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for IDP security auto-configuration.
 * Validates that security beans (JWT decoder, filters, authorities) are wired correctly.
 */
@IdpIntegrationTest
class SecurityAutoConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void securityAutoConfigurationLoads() {
        assertThat(context).isNotNull();
        assertThat(context.containsBean("securityAutoConfiguration")).isTrue();
    }

    @Test
    void jwtAuthoritiesConverterIsWired() {
        assertThat(context.containsBean("jwtAuthoritiesConverter")).isTrue();
    }

    @Test
    void principalExtractorIsWired() {
        assertThat(context.containsBean("idpPrincipalExtractor")).isTrue();
    }

    @Test
    void bearerTokenResolverIsWired() {
        assertThat(context.containsBean("idpBearerTokenResolver")).isTrue();
    }

    @Test
    void authenticationEntryPointIsWired() {
        assertThat(context.containsBean("idpAuthenticationEntryPoint")).isTrue();
    }

    @Test
    void accessDeniedHandlerIsWired() {
        assertThat(context.containsBean("idpAccessDeniedHandler")).isTrue();
    }

    @Test
    void securityFilterChainIsWired() {
        assertThat(context.containsBean("idpSecurityFilterChain")).isTrue();
    }
}

