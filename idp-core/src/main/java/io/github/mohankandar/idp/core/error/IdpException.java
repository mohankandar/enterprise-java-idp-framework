package io.github.mohankandar.idp.core.error;

import io.github.mohankandar.idp.core.api.ErrorDetail;

/**
 * Lightweight domain exception carrying an ErrorDetail and optional httpStatus.
 * Web layer maps this to an HTTP response; core remains Spring-agnostic.
 */
public class IdpException extends RuntimeException {
    private final ErrorDetail error;
    private final Integer httpStatus; // optional; can be null

    public IdpException(ErrorDetail error) {
        super(error == null ? null : error.getMessage());
        this.error = error;
        this.httpStatus = null;
    }

    public IdpException(ErrorDetail error, int httpStatus) {
        super(error == null ? null : error.getMessage());
        this.error = error;
        this.httpStatus = httpStatus;
    }

    public ErrorDetail getError() { return error; }
    public Integer getHttpStatus() { return httpStatus; }
}
