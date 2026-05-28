package io.github.mohankandar.idp.core.api;

/**
 * Marker interface for all error codes used in IDP.
 *
 * Framework-level enums (ErrorCode) and application-specific enums
 * can both implement this, and all can be passed to ErrorDetail.of(...).
 */
public interface IdpErrorCode {
  String getCode();
}
