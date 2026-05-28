package io.github.mohankandar.idp.platform;

import io.github.mohankandar.idp.platform.logging.CorrelationIdFilter;
import io.github.mohankandar.idp.platform.logging.LogHeadersFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAutoConfigurationContextRunnerTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PlatformAutoConfiguration.class));

    @Test
    void registersCorePlatformFiltersByDefault() {
        runner.run(context -> {
            assertThat(context).hasBean("correlationIdFilter");
            FilterRegistrationBean<?> registration = context.getBean("correlationIdFilter", FilterRegistrationBean.class);
            assertThat(registration.getFilter()).isInstanceOf(CorrelationIdFilter.class);
            assertThat(context).hasBean("idpMdcUserEnricherFilter");
            assertThat(context.getBean("idpMdcUserEnricherFilter")).isInstanceOf(OncePerRequestFilter.class);
            assertThat(context).doesNotHaveBean("logHeadersFilter");
        });
    }

    @Test
    void logHeadersFilterAppearsOnlyWhenExplicitlyEnabled() {
        runner.withPropertyValues(
                        "idp.logging.headers.enabled=true",
                        "idp.logging.headers.allowed=X-Correlation-Id,traceparent"
                )
                .run(context -> {
                    assertThat(context).hasBean("logHeadersFilter");
                    FilterRegistrationBean<?> registration = context.getBean("logHeadersFilter", FilterRegistrationBean.class);
                    assertThat(registration.getFilter()).isInstanceOf(LogHeadersFilter.class);
                });

        runner.run(context -> assertThat(context).doesNotHaveBean("logHeadersFilter"));
    }
}
