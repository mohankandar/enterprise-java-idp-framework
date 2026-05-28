package io.github.mohankandar.idp.test.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

/**
 * Docker-free integration test base retained for backward compatibility.
 *
 * <p>In this environment, integration tests are expected to provide their own test datasource
 * through the active test profile rather than Testcontainers. The class keeps the standard
 * IDP integration-test tag so Failsafe can execute subclasses via the parent group filter.
 */
@Tag("it")
@ActiveProfiles("test")
public abstract class IdpPostgresIntegrationTest {
}
