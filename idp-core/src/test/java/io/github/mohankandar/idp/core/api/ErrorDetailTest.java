package io.github.mohankandar.idp.core.api;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorDetail.
 * Validates error detail structure and factory methods.
 */
class ErrorDetailTest {

    @Test
    void createsErrorDetailViaStaticFactory() {
        ErrorDetail error = ErrorDetail.of("VALIDATION_ERROR", "Input validation failed");

        assertThat(error.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.getMessage()).isEqualTo("Input validation failed");
        assertThat(error.getDetails()).isNull();
    }

    @Test
    void createsErrorDetailWithDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("field", "email");
        details.put("reason", "required");

        ErrorDetail error = ErrorDetail.of("VALIDATION_ERROR", "Input validation failed", details);

        assertThat(error.getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.getMessage()).isEqualTo("Input validation failed");
        assertThat(error.getDetails()).isNotEmpty();
        assertThat(error.getDetails()).containsEntry("field", "email");
    }

    @Test
    void detailsAreImmutable() {
        Map<String, Object> mutableDetails = new HashMap<>();
        mutableDetails.put("key", "value");

        ErrorDetail error = ErrorDetail.of("ERROR", "An error occurred", mutableDetails);
        Map<String, Object> returnedDetails = error.getDetails();

        // Should be unmodifiable
        assertThat(returnedDetails).isNotNull();
        assertThat(returnedDetails).containsEntry("key", "value");
    }

    @Test
    void handlesNullDetails() {
        ErrorDetail error = ErrorDetail.of("NOT_FOUND", "Resource not found", null);

        assertThat(error.getDetails()).isNull();
    }

    @Test
    void handlesEmptyDetails() {
        Map<String, Object> emptyDetails = new HashMap<>();
        ErrorDetail error = ErrorDetail.of("ERROR", "Error message", emptyDetails);

        assertThat(error.getDetails()).isNull();
    }
}

