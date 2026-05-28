package io.github.mohankandar.idp.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthFilterTest {

    @Test
    void matchesSingleConfiguredApiKey() {
        SecurityProperties properties = new SecurityProperties();
        properties.getApiKey().setEnabled(true);
        properties.getApiKey().setHeader("X-API-Key");
        properties.getApiKey().setValue("secret-key");

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "secret-key");

        assertThat(filter.getPreAuthenticatedPrincipal(request)).isEqualTo("API-KEY");
    }

    @Test
    void returnsNullForBearerRequestsToAvoidConflictingAuthModes() {
        SecurityProperties properties = new SecurityProperties();
        properties.getApiKey().setEnabled(true);
        properties.getApiKey().setValue("secret-key");

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        request.addHeader("X-API-Key", "secret-key");

        assertThat(filter.getPreAuthenticatedPrincipal(request)).isNull();
    }

    @Test
    void resolvesNamedClientWhenClientKeyMatches() {
        SecurityProperties properties = new SecurityProperties();
        properties.getApiKey().setEnabled(true);
        SecurityProperties.ApiKey.Client client = new SecurityProperties.ApiKey.Client();
        client.setKeys(List.of("key-a", "key-b"));
        properties.getApiKey().setClients(Map.of("client-a", client));

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-API-Key", "key-b");

        assertThat(filter.getPreAuthenticatedPrincipal(request)).isEqualTo("client-a");
    }
}
