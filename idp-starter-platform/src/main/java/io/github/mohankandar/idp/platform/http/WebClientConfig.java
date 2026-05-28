package io.github.mohankandar.idp.platform.http;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Opinionated WebClient configuration:
 * - Applies IDP HTTP timeout defaults.
 * - Propagates correlationId via X-Correlation-Id header.
 */
@Configuration
@ConditionalOnClass(WebClient.class) // only activates if WebFlux is on the classpath
public class WebClientConfig {

  private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

  @Bean
  @ConditionalOnMissingBean(WebClient.Builder.class)
  public WebClient.Builder idpWebClientBuilder(IdpHttpProperties httpProperties,
      ObjectProvider<ExchangeFilterFunction> exchangeFilterFunctions) {

    IdpHttpProperties.TimeoutProperties timeout = httpProperties.getTimeout();

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.getConnectMs())
        .responseTimeout(Duration.ofMillis(timeout.getReadMs()));

    WebClient.Builder builder = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(correlationIdFilter());

    exchangeFilterFunctions.orderedStream()
        .filter(filter -> filter != null)
        .forEach(builder::filter);

    return builder;
  }

  private ExchangeFilterFunction correlationIdFilter() {
    return (request, next) -> {
      String cid = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
      if (cid == null || cid.isBlank()) {
        log.warn("Missing correlationId in MDC before WebClient outbound call");
        return next.exchange(request);
      }

      ClientRequest mutated = ClientRequest.from(request)
          .header(IdpLoggingConstants.CORRELATION_HEADER, cid)
          .build();

      return next.exchange(mutated);
    };
  }

  // Default WebClient bean that uses the above builder
  @Bean
  @ConditionalOnMissingBean(WebClient.class)
  public WebClient webClient(WebClient.Builder builder) {
    return builder.build();
  }
}
