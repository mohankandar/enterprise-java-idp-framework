package io.github.mohankandar.idp.security.propagation;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ConditionalOnClass(RestTemplate.class)
public class TokenPropagationRestTemplateConfig {

  @Bean
  @ConditionalOnMissingBean(name = "idpBearerTokenRestTemplateCustomizer")
  public RestTemplateCustomizer idpBearerTokenRestTemplateCustomizer() {
    return restTemplate -> restTemplate.getInterceptors().add((req, body, ex) -> {
      var ctx = SecurityContextHolder.getContext();
      var auth = (ctx != null) ? ctx.getAuthentication() : null;

      String token = null;
      if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jat) {
        token = jat.getToken().getTokenValue();
      } else if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication bta) {
        token = bta.getToken().getTokenValue();
      } else if (auth != null && auth.getCredentials() instanceof String s && !s.isBlank()) {
        token = s;
      }

      if (token != null && !token.isBlank()) {
        req.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
      }
      return ex.execute(req, body);
    });
  }
}
