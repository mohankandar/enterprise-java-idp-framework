package io.github.mohankandar.idp.demo.client.echo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class DownstreamEchoClient {

    private final WebClient webClient;

    public DownstreamEchoClient(WebClient.Builder builder,
                                RestTemplateBuilder ignored,
                                @Value("${server.port:8080}") int port) {
        // TokenPropagationConfig from the security starter should attach Authorization/API key
        this.webClient = builder.baseUrl("http://localhost:" + port).build();
    }

    public Map<?,?> callEcho() {
        return webClient.get().uri("/internal/echo")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
