package io.github.mohankandar.idp.demo.service;

import io.github.mohankandar.idp.demo.client.echo.ExternalEchoFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Demonstrates calling an external service via RestTemplate, WebClient, and Feign.
 */
@Service
public class ExternalEchoService {

  private final String baseUrl;
  private final RestTemplateBuilder restTemplateBuilder;
  private final WebClient webClient;
  private final ExternalEchoFeignClient feignClient;

  public ExternalEchoService(RestTemplateBuilder restTemplateBuilder,
      WebClient.Builder webClientBuilder,
      ExternalEchoFeignClient feignClient,
      @Value("${idp.partners.services.external-echo.rest.baseUrl:https://httpbin.org}") String baseUrl) {

    this.restTemplateBuilder = restTemplateBuilder;
    this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    this.feignClient = feignClient;
    this.baseUrl = baseUrl;
  }

  public Map<String, Object> callWithRestTemplate() {
    URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .path("/anything/resttemplate")
        .queryParam("source", "resttemplate")
        .build()
        .toUri();

    return restTemplateBuilder.build().getForObject(uri, Map.class);
  }

  public Map<String, Object> callWithWebClient() {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/anything/webclient")
            .queryParam("source", "webclient")
            .build())
        .retrieve()
        .bodyToMono(Map.class)
        .block();
  }

  public Map<String, Object> callWithFeign() {
    return feignClient.echo("feign");
  }
}
