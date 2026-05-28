package io.github.mohankandar.idp.soap.faults;

/** Partner returned a SOAP fault. */
public class PartnerFaultException extends PartnerSoapException {
    public PartnerFaultException(String partner, String operation, String message, Throwable cause) {
        super(partner, operation, message, cause);
    }
}
