package io.github.mohankandar.idp.platform.performance;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class IdpPerformanceAutoConfigurationContextRunnerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdpPerformanceAutoConfiguration.class))
            .withBean(SimpleMeterRegistry.class, SimpleMeterRegistry::new);

    @Test
    void loadsWithoutWebfluxOnClasspath() {
        runner.withClassLoader(new FilteredClassLoader("org.springframework.web.reactive.function.client"))
                .run(context -> {
                    assertThat(context).hasSingleBean(IdpPerformanceMetrics.class);
                    assertThat(context).hasSingleBean(IdpLayerPerformanceAspect.class);
                    assertThat(context).doesNotHaveBean("idpWebClientPerformanceFilter");
                });
    }
}

