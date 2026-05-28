package io.github.mohankandar.idp.platform.http;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Opinionated RestTemplate configuration:
 * - Applies IDP HTTP timeout defaults.
 * - Propagates correlationId via X-Correlation-Id header.
 */
@Configuration
public class RestTemplateConfig {

  private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

  @Bean
  @ConditionalOnMissingBean(RestTemplate.class)
  public RestTemplate restTemplate(RestTemplateBuilder builder,
      IdpHttpProperties httpProperties,
      ObjectProvider<RestTemplateCustomizer> customizersProvider) {

    IdpHttpProperties.TimeoutProperties timeout = httpProperties.getTimeout();

    Duration connectTimeout = Duration.ofMillis(timeout.getConnectMs());
    Duration readTimeout = Duration.ofMillis(timeout.getReadMs());

    RestTemplateCustomizer[] customizers = customizersProvider.orderedStream().toArray(RestTemplateCustomizer[]::new);

    return builder
        .additionalCustomizers(customizers)
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout)
        .additionalInterceptors((req, body, ex) -> {
          String cid = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
          if (cid != null && !cid.isBlank()) {
            req.getHeaders().add(IdpLoggingConstants.CORRELATION_HEADER, cid);
          } else {
            log.warn("Missing correlationId in MDC before RestTemplate outbound call");
          }
          return ex.execute(req, body);
        })
        .build();
  }
}
