package io.github.mohankandar.idp.soap.client;

/**
 * Creates configured CXF JAX-WS proxy clients for partner SOAP ports.
 */
public interface SoapClientFactory {

    /**
     * Create a SOAP port client for the configured partner.
     *
     * @param partnerName configured key under idp.partners.services (type=SOAP)
     * @param portType generated JAX-WS port interface
     */
    <T> T createClient(String partnerName, Class<T> portType);
}
