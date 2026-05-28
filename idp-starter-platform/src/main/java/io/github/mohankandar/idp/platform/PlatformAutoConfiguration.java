package io.github.mohankandar.idp.platform;

import io.github.mohankandar.idp.platform.http.IdpHttpProperties;
import io.github.mohankandar.idp.platform.logging.CorrelationIdFilter;
import io.github.mohankandar.idp.platform.logging.LogHeadersFilter;
import io.github.mohankandar.idp.platform.logging.MdcUserEnricherFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@AutoConfiguration
@EnableConfigurationProperties({
    IdpHttpProperties.class
})
public class PlatformAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
    var bean = new FilterRegistrationBean<>(new CorrelationIdFilter());
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    bean.addUrlPatterns("/*");
    return bean;
  }



  @Bean
  @ConditionalOnProperty(prefix = "idp.logging.headers", name = "enabled", havingValue = "true")
  @ConditionalOnMissingBean(name = "idpLogHeadersFilter")
  public FilterRegistrationBean<LogHeadersFilter> logHeadersFilter(
      @Value("${idp.logging.headers.allowed:X-Correlation-Id,X-Request-Id,traceparent,tracestate,User-Agent}")
      String allowedHeaders) {
    var bean = new FilterRegistrationBean<>(new LogHeadersFilter(parseAllowedHeaders(allowedHeaders)));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    bean.addUrlPatterns("/*");
    bean.setName("idpLogHeadersFilter");
    return bean;
  }

  @Bean(name = "idpMdcUserEnricherFilter")
  @ConditionalOnMissingBean(name = "idpMdcUserEnricherFilter")
  public OncePerRequestFilter idpMdcUserEnricherFilter() {
    return new MdcUserEnricherFilter();
  }


  private static Set<String> parseAllowedHeaders(String allowedHeaders) {
    Set<String> result = new LinkedHashSet<>();
    Arrays.stream(allowedHeaders.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .map(value -> value.toLowerCase(Locale.ROOT))
        .forEach(result::add);
    return result;
  }

}
