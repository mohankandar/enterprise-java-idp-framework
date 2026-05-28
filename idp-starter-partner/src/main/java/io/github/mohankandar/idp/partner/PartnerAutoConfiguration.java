package io.github.mohankandar.idp.partner;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohankandar.idp.partner.auth.OAuthClientCredentialsTokenService;
import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import io.github.mohankandar.idp.partner.rest.PartnerWebClientFactory;
import io.github.mohankandar.idp.partner.validation.PartnerPropertiesValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@EnableConfigurationProperties(IdpPartnerProperties.class)
public class PartnerAutoConfiguration {

    @Bean
    public PartnerServiceRegistry partnerServiceRegistry(IdpPartnerProperties props) {
        return new PartnerServiceRegistry(props);
    }

    @Bean
    public PartnerPropertiesValidator partnerPropertiesValidator(IdpPartnerProperties props) {
        return new PartnerPropertiesValidator(props);
    }

    @Bean
    public OAuthClientCredentialsTokenService oauthClientCredentialsTokenService(
        PartnerServiceRegistry registry,
        WebClient.Builder builder,
        ObjectMapper objectMapper) {
        return new OAuthClientCredentialsTokenService(registry, builder, objectMapper);
    }

    @Bean
    public PartnerWebClientFactory partnerWebClientFactory(
        PartnerServiceRegistry registry,
        WebClient.Builder builder,
        OAuthClientCredentialsTokenService tokenService) {
        return new PartnerWebClientFactory(registry, builder, tokenService);
    }
}
