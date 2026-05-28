package io.github.mohankandar.idp.soap.interceptors;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.phase.Phase;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Adds the current MDC trace id as a SOAP header for partner visibility and troubleshooting.
 *
 * Safe-by-default: if MDC is missing or XML building fails, it does nothing.
 */
public class CorrelationIdSoapOutInterceptor extends AbstractSoapInterceptor {

    /** Align this with your logging MDC key. */
    public static final String MDC_TRACE_ID = "traceId";

    private static final QName HEADER_QNAME = new QName("urn:idp-framework:soap", "CorrelationId");

    public CorrelationIdSoapOutInterceptor() {
        super(Phase.PREPARE_SEND);
    }

    @Override
    public void handleMessage(SoapMessage message) {
        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            return;
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element el = doc.createElementNS(HEADER_QNAME.getNamespaceURI(), HEADER_QNAME.getLocalPart());
            el.setTextContent(traceId);

            message.getHeaders().add(new Header(HEADER_QNAME, el));
        } catch (ParserConfigurationException ex) {
            // never break partner call due to header injection
        }
    }
}
