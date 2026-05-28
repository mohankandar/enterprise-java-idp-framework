package io.github.mohankandar.idp.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Minimal, always-safe configuration that other starters can rely on.
 * - Provides a system {@link Clock} bean
 * - Binds IdpProperties (app name, logging, feature flags)
 */
@AutoConfiguration
@EnableConfigurationProperties(IdpProperties.class)
public class IdpAutoConfiguration {

    @Bean
    public Clock idpSystemClock() {
        return Clock.systemUTC();
    }
}
