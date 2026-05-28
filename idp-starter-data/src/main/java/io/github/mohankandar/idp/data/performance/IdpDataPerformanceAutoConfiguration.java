package io.github.mohankandar.idp.data.performance;

import io.github.mohankandar.idp.platform.performance.IdpPerformanceMetrics;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnBean({DataSource.class, IdpPerformanceMetrics.class, IdpPerformanceProperties.class})
public class IdpDataPerformanceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdpDataSourcePerformanceBeanPostProcessor idpDataSourcePerformanceBeanPostProcessor(
        IdpPerformanceProperties properties,
        IdpPerformanceMetrics metrics) {
        return new IdpDataSourcePerformanceBeanPostProcessor(properties, metrics);
    }
}
