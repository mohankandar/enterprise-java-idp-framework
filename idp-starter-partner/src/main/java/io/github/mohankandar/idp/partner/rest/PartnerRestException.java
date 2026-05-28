package io.github.mohankandar.idp.partner.rest;


/**
 * Standard exception for REST partner failures.
 */
public class PartnerRestException extends RuntimeException {

    private final String partnerName;
    private final String method;
    private final String url;
    private final Integer status;

    public PartnerRestException(String partnerName, String method, String url, Integer status, String message, Throwable cause) {
        super(message, cause);
        this.partnerName = partnerName;
        this.method = method;
        this.url = url;
        this.status = status;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Integer getStatus() {
        return status;
    }
}
