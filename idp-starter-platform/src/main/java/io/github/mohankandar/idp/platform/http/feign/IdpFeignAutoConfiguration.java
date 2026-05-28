package io.github.mohankandar.idp.platform.http.feign;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class IdpFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "idpFeignCorrelationRequestInterceptor")
    public RequestInterceptor idpFeignCorrelationRequestInterceptor() {
        return new IdpFeignCorrelationRequestInterceptor();
    }
}
