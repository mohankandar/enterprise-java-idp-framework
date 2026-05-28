package io.github.mohankandar.idp.security.propagation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TokenPropagationWebClientConfigTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addsBearerAuthorizationHeaderFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "token-xyz")
        );

        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://example.test"))
                .build();

        new TokenPropagationWebClientConfig().bearerPropagationFilter()
                .filter(request, req -> {
                    captured.set(req);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                })
                .block();

        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token-xyz");
    }
}
