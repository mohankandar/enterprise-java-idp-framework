package io.github.mohankandar.idp.partner.feign;

import io.github.mohankandar.idp.partner.auth.OAuthClientCredentialsTokenService;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnBean(PartnerServiceRegistry.class)
public class PartnerFeignAutoConfiguration {

    @Bean
    public RequestInterceptor partnerAuthRequestInterceptor(PartnerServiceRegistry registry, OAuthClientCredentialsTokenService tokenService) {
        return new PartnerFeignRequestInterceptor(registry, tokenService);
    }
}