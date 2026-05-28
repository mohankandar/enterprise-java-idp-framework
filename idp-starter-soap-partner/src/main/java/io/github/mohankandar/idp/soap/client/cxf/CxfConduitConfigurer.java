package io.github.mohankandar.idp.soap.client.cxf;

import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class CxfConduitConfigurer {

    private final ResourceLoader resourceLoader;

    public CxfConduitConfigurer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void configure(HTTPConduit conduit, String partnerName, IdpPartnerProperties.SoapProperties props) {
        configureTimeouts(conduit, props);
        configureProxy(conduit, props);
        configureTlsTruststore(conduit, partnerName, props);
    }

    private void configureTimeouts(HTTPConduit conduit, IdpPartnerProperties.SoapProperties props) {
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(props.getConnectTimeoutMs());
        policy.setReceiveTimeout(props.getReadTimeoutMs());
        policy.setAllowChunking(true);
        conduit.setClient(policy);
    }

    private void configureProxy(HTTPConduit conduit, IdpPartnerProperties.SoapProperties props) {
        IdpPartnerProperties.ProxyProperties proxy = props.getProxy();
        if (proxy == null || !proxy.isEnabled()) {
            return;
        }

        conduit.getClient().setProxyServer(proxy.getHost());
        conduit.getClient().setProxyServerPort(proxy.getPort());

        if (StringUtils.hasText(proxy.getUsername())) {
            ProxyAuthorizationPolicy pap = new ProxyAuthorizationPolicy();
            pap.setUserName(proxy.getUsername());
            pap.setPassword(proxy.getPassword());
            conduit.setProxyAuthorization(pap);
        }
    }

    private void configureTlsTruststore(HTTPConduit conduit, String partnerName, IdpPartnerProperties.SoapProperties props) {
        IdpPartnerProperties.TlsProperties tls = props.getTls();
        if (tls == null || !StringUtils.hasText(tls.getTruststore())) {
            return;
        }

        try {
            Resource tsResource = resourceLoader.getResource(tls.getTruststore());
            KeyStore trustStore = KeyStore.getInstance(tls.getTruststoreType());

            char[] pwd = tls.getTruststorePassword() != null ? tls.getTruststorePassword().toCharArray() : null;
            try (InputStream in = tsResource.getInputStream()) {
                trustStore.load(in, pwd);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            TLSClientParameters tlsParams = new TLSClientParameters();
            tlsParams.setTrustManagers(tmf.getTrustManagers());
            tlsParams.setDisableCNCheck(tls.isDisableCnCheck());

            conduit.setTlsClientParameters(tlsParams);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure truststore for SOAP partner '" + partnerName + "'", e);
        }
    }
}