package io.github.mohankandar.idp.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorDetail {

    private final String code;
    private final String message;
    private final Map<String, Object> details;

    private ErrorDetail(String code, String message, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        if (details == null || details.isEmpty()) {
            this.details = null;
        } else {
            this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
        }
    }

    // ---- Existing string-based factory methods (unchanged) ----

    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, message, null);
    }

    public static ErrorDetail of(String code, String message, Map<String, Object> details) {
        return new ErrorDetail(code, message, details);
    }

    // ---- New generic factory methods using IdpErrorCode ----

    public static ErrorDetail of(IdpErrorCode code, String message) {
        Objects.requireNonNull(code, "code must not be null");
        return new ErrorDetail(code.getCode(), message, null);
    }

    public static ErrorDetail of(IdpErrorCode code, String message, Map<String, Object> details) {
        Objects.requireNonNull(code, "code must not be null");
        return new ErrorDetail(code.getCode(), message, details);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
