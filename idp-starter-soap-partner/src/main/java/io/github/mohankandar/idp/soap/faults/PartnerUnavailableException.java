package io.github.mohankandar.idp.soap.faults;

/** Connectivity / timeout / TLS category. */
public class PartnerUnavailableException extends PartnerSoapException {
    public PartnerUnavailableException(String partner, String operation, String message, Throwable cause) {
        super(partner, operation, message, cause);
    }
}
