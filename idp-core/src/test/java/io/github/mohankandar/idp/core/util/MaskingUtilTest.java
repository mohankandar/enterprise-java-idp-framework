package io.github.mohankandar.idp.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive unit tests for MaskingUtil.
 * Validates all masking patterns: SSN, credit card, bearer tokens, app tokens.
 */
class MaskingUtilTest {

    // ---- SSN masking ----

    @Test
    void masksSSNWithDashes() {
        String result = MaskingUtil.maskAll("SSN: 123-45-6789");
        assertThat(result).doesNotContain("123-45").contains("***-**-6789");
    }

    @Test
    void masksSSNWithSpaces() {
        String result = MaskingUtil.maskAll("SSN: 123 45 6789");
        assertThat(result).doesNotContain("123 45").contains("6789");
    }

    @Test
    void masksSSNWithoutDelimiters() {
        String result = MaskingUtil.maskAll("ssn=123456789");
        assertThat(result).doesNotContain("123456789");
    }

    // ---- Bearer token masking ----

    @Test
    void masksBearerToken() {
        String result = MaskingUtil.maskAll("Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.abc.def");
        assertThat(result).contains("Bearer *****");
        assertThat(result).doesNotContain("eyJhbGciOiJSUzI1NiJ9");
    }

    @Test
    void masksBearerTokenCaseInsensitive() {
        String result = MaskingUtil.maskAll("BEARER abc123token");
        assertThat(result).contains("*****");
        assertThat(result).doesNotContain("abc123token");
    }

    // ---- App token masking ----

    @Test
    void masksIdpAppToken() {
        String result = MaskingUtil.maskAll("idp.app.token: \"super-secret-token\"");
        assertThat(result).contains("*****");
        assertThat(result).doesNotContain("super-secret-token");
    }

    // ---- Null / blank handling ----

    @Test
    void returnsNullForNullInput() {
        assertThat(MaskingUtil.maskAll(null)).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void returnsInputForBlankInput(String input) {
        assertThat(MaskingUtil.maskAll(input)).isEqualTo(input);
    }

    @Test
    void doesNotAlterTextWithNoSensitiveData() {
        String clean = "Hello World: John Doe, user ID 999";
        assertThat(MaskingUtil.maskAll(clean)).isEqualTo(clean);
    }

    // ---- last4 ----

    @Test
    void masksAllButLast4Chars() {
        assertThat(MaskingUtil.last4("4111111111111111")).isEqualTo("************1111");
    }

    @Test
    void returnsAllStarsWhenValueIs4OrFewer() {
        assertThat(MaskingUtil.last4("1234")).isEqualTo("****");
        assertThat(MaskingUtil.last4("12")).isEqualTo("****");
    }

    @Test
    void last4ThrowsOnNullInput() {
        assertThatThrownBy(() -> MaskingUtil.last4(null))
            .isInstanceOf(NullPointerException.class);
    }
}

