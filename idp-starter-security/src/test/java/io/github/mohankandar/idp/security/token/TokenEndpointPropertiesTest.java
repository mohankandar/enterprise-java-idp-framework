package io.github.mohankandar.idp.security.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TokenEndpointProperties.
 * Validates that security token configuration is properly structured.
 */
class TokenEndpointPropertiesTest {

    @Test
    void createsInstanceWithHmacSecret() {
        TokenEndpointProperties props = new TokenEndpointProperties();
        props.setHmacSecret("this-is-a-secret-key-that-is-long-enough");

        assertThat(props.getHmacSecret()).isEqualTo("this-is-a-secret-key-that-is-long-enough");
    }

    @Test
    void createsInstanceWithIssuer() {
        TokenEndpointProperties props = new TokenEndpointProperties();
        props.setIssuer("custom-issuer");

        assertThat(props.getIssuer()).isEqualTo("custom-issuer");
    }

    @Test
    void createsInstanceWithEnabled() {
        TokenEndpointProperties props = new TokenEndpointProperties();
        props.setEnabled(true);

        assertThat(props.isEnabled()).isTrue();
    }

    @Test
    void defaultsToDisabled() {
        TokenEndpointProperties props = new TokenEndpointProperties();

        assertThat(props.isEnabled()).isFalse();
    }

    @Test
    void setsExpirySeconds() {
        TokenEndpointProperties props = new TokenEndpointProperties();
        props.setExpirySeconds(3600);

        assertThat(props.getExpirySeconds()).isEqualTo(3600);
    }

    @Test
    void defaultExpiryIs900Seconds() {
        TokenEndpointProperties props = new TokenEndpointProperties();

        assertThat(props.getExpirySeconds()).isEqualTo(900);
    }
}

