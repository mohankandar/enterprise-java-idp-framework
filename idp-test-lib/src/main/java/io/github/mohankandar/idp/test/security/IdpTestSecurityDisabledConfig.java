package io.github.mohankandar.idp.test.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Optional test configuration to disable authentication/authorization in full-context tests.
 *
 * <p>Note: {@code @IdpWebMvcTest} already disables filters via {@code addFilters=false}.
 */
@TestConfiguration
public class IdpTestSecurityDisabledConfig {

  @Bean
  SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}
