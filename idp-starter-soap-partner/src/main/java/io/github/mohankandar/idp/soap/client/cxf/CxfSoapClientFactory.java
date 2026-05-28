package io.github.mohankandar.idp.soap.client.cxf;

import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import io.github.mohankandar.idp.soap.client.SoapClientFactory;
import io.github.mohankandar.idp.soap.interceptors.CorrelationIdSoapOutInterceptor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CxfSoapClientFactory implements SoapClientFactory {

    private final PartnerServiceRegistry registry;
    private final CxfConduitConfigurer conduitConfigurer;
    private final CorrelationIdSoapOutInterceptor correlationInterceptor;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Injected interceptor is a framework-managed collaborator reference, not mutable domain state."
    )
    public CxfSoapClientFactory(
        PartnerServiceRegistry registry,
        CxfConduitConfigurer conduitConfigurer,
        CorrelationIdSoapOutInterceptor correlationInterceptor
    ) {
        this.registry = registry;
        this.conduitConfigurer = conduitConfigurer;
        this.correlationInterceptor = correlationInterceptor;
    }

    @Override
    public <T> T createClient(String partnerName, Class<T> portType) {
        IdpPartnerProperties.SoapProperties props = registry.soap(partnerName);
        if (!StringUtils.hasText(props.getEndpoint())) {
            throw new IllegalStateException("SOAP partner '" + partnerName + "' endpoint is missing");
        }

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(portType);
        factory.setAddress(props.getEndpoint());
        factory.getOutInterceptors().add(correlationInterceptor);

        @SuppressWarnings("unchecked")
        T proxy = (T) factory.create();

        Client client = ClientProxy.getClient(proxy);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduitConfigurer.configure(conduit, partnerName, props);

        applyHttpHeaders(client, props);

        return proxy;
    }

    private void applyHttpHeaders(Client client, IdpPartnerProperties.SoapProperties props) {
        Map<String, String> headers = props.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }

        Map<String, List<String>> protocolHeaders = headers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> List.of(e.getValue())));

        client.getRequestContext().put(Message.PROTOCOL_HEADERS, protocolHeaders);
    }
}