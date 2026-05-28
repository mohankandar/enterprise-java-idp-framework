package io.github.mohankandar.idp.soap.faults;

import jakarta.xml.ws.soap.SOAPFaultException;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Translates low-level SOAP/network exceptions into stable IDP partner exception types.
 */
public class SoapFaultTranslator {

    public RuntimeException translate(String partner, String operation, Exception ex) {
        if (hasCause(ex, SocketTimeoutException.class)) {
            return new PartnerUnavailableException(partner, operation,
                operation + " timed out calling SOAP partner '" + partner + "'", ex);
        }
        if (hasCause(ex, ConnectException.class)) {
            return new PartnerUnavailableException(partner, operation,
                operation + " connection failed calling SOAP partner '" + partner + "'", ex);
        }
        if (hasCause(ex, SSLHandshakeException.class)) {
            return new PartnerUnavailableException(partner, operation,
                operation + " TLS handshake failed calling SOAP partner '" + partner + "'", ex);
        }
        if (hasCause(ex, SOAPFaultException.class)) {
            return new PartnerFaultException(partner, operation,
                operation + " SOAP fault returned from partner '" + partner + "'", ex);
        }

        return new PartnerSoapException(partner, operation,
            operation + " failed calling SOAP partner '" + partner + "'", ex);
    }

    private boolean hasCause(Throwable t, Class<? extends Throwable> type) {
        Throwable cur = t;
        while (cur != null) {
            if (type.isInstance(cur)) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
