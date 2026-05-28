package io.github.mohankandar.idp.security.token;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@AutoConfiguration
@EnableConfigurationProperties({TokenEndpointProperties.class, IdpTokenProperties.class})
@Conditional(TokenEndpointProfileCondition.class)
@ConditionalOnProperty(prefix = "idp.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdpTokenEndpointAutoConfiguration {

  @Bean
  @ConditionalOnProperty(
      prefix = "idp.security.token-endpoint",
      name = "enabled",
      havingValue = "true"
  )
  public TokenEndpointController tokenEndpointController(
      TokenEndpointProperties tokenEndpointProperties,
      IdpTokenProperties idpTokenProperties
  ) {
    return new TokenEndpointController(tokenEndpointProperties, idpTokenProperties);
  }
}
