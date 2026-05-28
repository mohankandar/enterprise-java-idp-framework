package io.github.mohankandar.idp.soap.faults;

/** Base runtime exception for outbound partner SOAP failures. */
public class PartnerSoapException extends RuntimeException {

    private final String partner;
    private final String operation;

    public PartnerSoapException(String partner, String operation, String message, Throwable cause) {
        super(message, cause);
        this.partner = partner;
        this.operation = operation;
    }

    public String getPartner() {
        return partner;
    }

    public String getOperation() {
        return operation;
    }
}
