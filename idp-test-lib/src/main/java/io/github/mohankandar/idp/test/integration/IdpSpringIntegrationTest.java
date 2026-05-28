package io.github.mohankandar.idp.test.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

/**
 * Standard Docker-free IDP integration test base.
 *
 * <p>Services should supply datasource and other runtime-specific test settings through the
 * active {@code test} profile. The base contributes the {@code it} tag used by Failsafe.
 */
@Tag("it")
@ActiveProfiles("test")
public abstract class IdpSpringIntegrationTest {
}
