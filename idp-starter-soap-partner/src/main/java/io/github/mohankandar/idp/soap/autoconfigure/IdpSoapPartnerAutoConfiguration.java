package io.github.mohankandar.idp.soap.autoconfigure;

import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import io.github.mohankandar.idp.soap.client.SoapClientFactory;
import io.github.mohankandar.idp.soap.client.cxf.CxfConduitConfigurer;
import io.github.mohankandar.idp.soap.client.cxf.CxfSoapClientFactory;
import io.github.mohankandar.idp.soap.faults.SoapFaultTranslator;
import io.github.mohankandar.idp.soap.interceptors.CorrelationIdSoapOutInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
public class IdpSoapPartnerAutoConfiguration {

    @Bean
    public SoapFaultTranslator soapFaultTranslator() {
        return new SoapFaultTranslator();
    }

    @Bean
    public CorrelationIdSoapOutInterceptor correlationIdSoapOutInterceptor() {
        return new CorrelationIdSoapOutInterceptor();
    }

    @Bean
    public CxfConduitConfigurer cxfConduitConfigurer(ResourceLoader resourceLoader) {
        return new CxfConduitConfigurer(resourceLoader);
    }

    @Bean
    public SoapClientFactory soapClientFactory(
        PartnerServiceRegistry registry,
        CxfConduitConfigurer conduitConfigurer,
        CorrelationIdSoapOutInterceptor correlationInterceptor
    ) {
        return new CxfSoapClientFactory(registry, conduitConfigurer, correlationInterceptor);
    }
}
