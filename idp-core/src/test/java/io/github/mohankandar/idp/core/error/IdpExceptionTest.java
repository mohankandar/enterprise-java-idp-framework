package io.github.mohankandar.idp.core.error;

import io.github.mohankandar.idp.core.api.ErrorDetail;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for IdpException.
 */
class IdpExceptionTest {

    @Test
    void createsExceptionWithErrorDetail() {
        ErrorDetail error = ErrorDetail.of("NOT_FOUND", "Resource not found");
        IdpException ex = new IdpException(error);

        assertThat(ex.getError()).isNotNull();
        assertThat(ex.getError().getCode()).isEqualTo("NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("Resource not found");
        assertThat(ex.getHttpStatus()).isNull();
    }

    @Test
    void createsExceptionWithHttpStatus() {
        ErrorDetail error = ErrorDetail.of("UNAUTHORIZED", "Not authorized");
        IdpException ex = new IdpException(error, 401);

        assertThat(ex.getError().getCode()).isEqualTo("UNAUTHORIZED");
        assertThat(ex.getHttpStatus()).isEqualTo(401);
    }

    @Test
    void createsExceptionWithInternalServerErrorStatus() {
        ErrorDetail error = ErrorDetail.of("INTERNAL_ERROR", "Something broke");
        IdpException ex = new IdpException(error, 500);

        assertThat(ex.getHttpStatus()).isEqualTo(500);
    }

    @Test
    void messageComesFromErrorDetail() {
        ErrorDetail error = ErrorDetail.of("ERR", "Detailed message here");
        IdpException ex = new IdpException(error);

        assertThat(ex.getMessage()).isEqualTo("Detailed message here");
    }

    @Test
    void isRuntimeException() {
        ErrorDetail error = ErrorDetail.of("ERR", "error");
        assertThat(new IdpException(error)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void httpStatusIsNullWhenNotProvided() {
        ErrorDetail error = ErrorDetail.of("ERR", "error");
        IdpException ex = new IdpException(error);
        assertThat(ex.getHttpStatus()).isNull();
    }
}

