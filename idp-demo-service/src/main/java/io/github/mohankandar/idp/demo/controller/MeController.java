package io.github.mohankandar.idp.demo.controller;

import io.github.mohankandar.idp.core.api.ApiResponse;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.identity.IdpIdentity;
import io.github.mohankandar.idp.identity.IdpIdentityClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns the current identity wrapped in the standard IDP ApiResponse envelope.
 * If no identity is present in the context, returns 401 with ErrorCode.UNAUTHORIZED.
 */
@RestController
public class MeController {

    private final IdpIdentityClient identityClient;

    public MeController(IdpIdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    @GetMapping("/api/me")
    public ResponseEntity<ApiResponse<IdpIdentity>> me() {
        return identityClient.current()
            .map(identity -> ResponseEntity.ok(ApiResponse.ok(identity)))
            .orElseGet(() -> {
                ErrorDetail error = ErrorDetail.of(
                    ErrorCode.UNAUTHORIZED,
                    "No authenticated user is available in the current context.",
                    null
                );
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(error));
            });
    }
}
