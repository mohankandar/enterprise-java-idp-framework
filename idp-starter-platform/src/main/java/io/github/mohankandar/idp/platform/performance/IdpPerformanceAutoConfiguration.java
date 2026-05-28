package io.github.mohankandar.idp.platform.performance;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(IdpPerformanceProperties.class)
public class IdpPerformanceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public IdpPerformanceMetrics idpPerformanceMetrics(MeterRegistry meterRegistry) {
        return new IdpPerformanceMetrics(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(IdpPerformanceMetrics.class)
    public IdpLayerPerformanceAspect idpLayerPerformanceAspect(
        IdpPerformanceProperties properties,
        IdpPerformanceMetrics metrics) {
        return new IdpLayerPerformanceAspect(properties, metrics);
    }

    @Bean
    @ConditionalOnMissingBean(name = "idpRestTemplatePerformanceCustomizer")
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnBean(IdpPerformanceMetrics.class)
    public IdpRestTemplatePerformanceCustomizer idpRestTemplatePerformanceCustomizer(
        IdpPerformanceProperties properties,
        IdpPerformanceMetrics metrics) {
        return new IdpRestTemplatePerformanceCustomizer(properties, metrics);
    }

    @Bean(name = "idpWebClientPerformanceFilter")
    @ConditionalOnMissingBean(name = "idpWebClientPerformanceFilter")
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    @ConditionalOnBean(IdpPerformanceMetrics.class)
    public Object idpWebClientPerformanceFilter(
        IdpPerformanceProperties properties,
        IdpPerformanceMetrics metrics) {
        return new IdpWebClientPerformanceFilter(properties, metrics);
    }
}
