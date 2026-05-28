package io.github.mohankandar.idp.security.propagation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TokenPropagationRestTemplateConfigTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addsBearerAuthorizationHeaderFromAuthenticationCredentials() throws IOException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "token-123")
        );

        RestTemplateCustomizer customizer = new TokenPropagationRestTemplateConfig().idpBearerTokenRestTemplateCustomizer();
        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, "/demo");
        ClientHttpRequestExecution execution = (req, body) -> {
            assertThat(req.getHeaders().getFirst("Authorization")).isEqualTo("Bearer token-123");
            return new NoOpResponse();
        };

        restTemplate.getInterceptors().get(0).intercept(request, new byte[0], execution);
    }

    private static class NoOpResponse implements ClientHttpResponse {
        @Override public org.springframework.http.HttpStatusCode getStatusCode() { return org.springframework.http.HttpStatus.OK; }
        @Override public String getStatusText() { return "OK"; }
        @Override public void close() { }
        @Override public org.springframework.http.HttpHeaders getHeaders() { return new org.springframework.http.HttpHeaders(); }
        @Override public java.io.InputStream getBody() { return java.io.InputStream.nullInputStream(); }
    }
}
