package io.github.mohankandar.idp.core.api;

public enum ErrorCode implements IdpErrorCode {

  // 4xx – client errors
  VALIDATION_ERROR("VALIDATION_ERROR"),
  CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION"),
  BAD_REQUEST("BAD_REQUEST"),
  UNAUTHORIZED("UNAUTHORIZED"),
  FORBIDDEN("FORBIDDEN"),
  NOT_FOUND("NOT_FOUND"),
  CONFLICT("CONFLICT"),
  TOO_MANY_REQUESTS("TOO_MANY_REQUESTS"),

  // 5xx – server / downstream errors
  DOWNSTREAM_ERROR("DOWNSTREAM_ERROR"),
  UNEXPECTED_ERROR("UNEXPECTED_ERROR");

  private final String code;

  ErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }
}
