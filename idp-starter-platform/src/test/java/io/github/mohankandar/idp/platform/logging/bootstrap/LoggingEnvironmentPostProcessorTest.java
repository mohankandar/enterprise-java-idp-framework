package io.github.mohankandar.idp.platform.logging.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingEnvironmentPostProcessorTest {

    private final LoggingEnvironmentPostProcessor processor = new LoggingEnvironmentPostProcessor();
    private String surefireTestClasspath;
    private String failsafeTestClasspath;

    @BeforeEach
    void captureJvmTestMarkers() {
        surefireTestClasspath = System.getProperty(LoggingEnvironmentPostProcessor.SUREFIRE_TEST_CLASSPATH);
        failsafeTestClasspath = System.getProperty(LoggingEnvironmentPostProcessor.FAILSAFE_TEST_CLASSPATH);
    }

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(LoggingEnvironmentPostProcessor.CATALINA_BASE);
        System.clearProperty(LoggingEnvironmentPostProcessor.LOG_PATH);
        System.clearProperty(LoggingEnvironmentPostProcessor.LOG_FILE_ENABLED);
        System.clearProperty(LoggingEnvironmentPostProcessor.LOG_CONSOLE_ENABLED);
        System.clearProperty(LoggingEnvironmentPostProcessor.RESOLVED_APP_ENV);
        restoreProperty(LoggingEnvironmentPostProcessor.SUREFIRE_TEST_CLASSPATH, surefireTestClasspath);
        restoreProperty(LoggingEnvironmentPostProcessor.FAILSAFE_TEST_CLASSPATH, failsafeTestClasspath);
    }

    @Test
    void testRuntimeIsAlwaysConsoleOnly() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "demo-app")
                .withProperty(LoggingEnvironmentPostProcessor.SPRING_BOOT_TEST_BOOTSTRAPPER, "true")
                .withProperty("spring.profiles.active", "test");

        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_FILE_ENABLED)).isEqualTo("false");
        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_CONSOLE_ENABLED)).isEqualTo("true");
        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_PATH)).isNull();
    }

    @Test
    void tomcatRuntimeUsesCatalinaLogsDirectory() throws Exception {
        clearJvmTestMarkers();
        Path catalinaBase = Files.createTempDirectory("idp-tomcat-base");
        System.setProperty(LoggingEnvironmentPostProcessor.CATALINA_BASE, catalinaBase.toString());

        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "demo-app")
                .withProperty("spring.profiles.active", "prod");

        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_FILE_ENABLED)).isEqualTo("true");
        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_CONSOLE_ENABLED)).isEqualTo("false");
        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_PATH))
                .isEqualTo(catalinaBase.resolve("logs").toString());
    }

    @Test
    void standaloneNonLocalRuntimeUsesDerivedLogsFolder() {
        clearJvmTestMarkers();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "demo-app")
                .withProperty("spring.profiles.active", "qa");

        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_FILE_ENABLED)).isEqualTo("true");
        assertThat(System.getProperty(LoggingEnvironmentPostProcessor.LOG_CONSOLE_ENABLED)).isEqualTo("false");
        String normalizedLogPath = System.getProperty(LoggingEnvironmentPostProcessor.LOG_PATH)
                .replace('\\', '/');
        assertThat(normalizedLogPath).endsWith("logs/demo-app/qa");
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    private static void clearJvmTestMarkers() {
        System.clearProperty(LoggingEnvironmentPostProcessor.SUREFIRE_TEST_CLASSPATH);
        System.clearProperty(LoggingEnvironmentPostProcessor.FAILSAFE_TEST_CLASSPATH);
    }
}
