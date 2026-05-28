package io.github.mohankandar.idp.data.performance;

import io.github.mohankandar.idp.platform.performance.IdpPerformanceMetrics;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class IdpDataPerformanceAutoConfigurationContextRunnerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdpDataPerformanceAutoConfiguration.class));

    @Test
    void registersBeanPostProcessorWhenDatasourceAndPerformanceBeansExist() {
        runner.withUserConfiguration(RequiredBeansConfig.class)
                .run(context -> assertThat(context).hasSingleBean(IdpDataSourcePerformanceBeanPostProcessor.class));
    }

    @Test
    void backsOffWhenPerformanceBeansAreMissing() {
        runner.withUserConfiguration(DataSourceOnlyConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(IdpDataSourcePerformanceBeanPostProcessor.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class RequiredBeansConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource();
        }

        @Bean
        IdpPerformanceProperties idpPerformanceProperties() {
            IdpPerformanceProperties properties = new IdpPerformanceProperties();
            properties.setEnabled(true);
            properties.setDbEnabled(true);
            return properties;
        }

        @Bean
        IdpPerformanceMetrics idpPerformanceMetrics() {
            return new IdpPerformanceMetrics(new SimpleMeterRegistry());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class DataSourceOnlyConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource();
        }
    }
}
