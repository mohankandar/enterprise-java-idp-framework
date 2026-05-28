package io.github.mohankandar.idp.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@AutoConfiguration
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MethodSecurityAutoConfiguration {
}
