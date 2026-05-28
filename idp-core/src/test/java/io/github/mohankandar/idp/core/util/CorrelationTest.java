package io.github.mohankandar.idp.core.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Correlation utility.
 */
class CorrelationTest {

    @Test
    void generatesUniqueCorrelationId() {
        String id1 = Correlation.newId();
        String id2 = Correlation.newId();

        assertThat(id1).isNotBlank();
        assertThat(id2).isNotBlank();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void correlationIdIsUUIDFormat() {
        String id = Correlation.newId();
        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        assertThat(id).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void generatesNonNullId() {
        assertThat(Correlation.newId()).isNotNull();
    }
}

