package io.github.mohankandar.idp.platform.logging.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves logging mode, environment label, and log path before Logback initializes.
 * <p>
 * Design rules:
 * <ol>
 *   <li>Test bootstrap and JVM test runs are always console-only.</li>
 *   <li>Standalone developer runs using local-like profiles are console-only.</li>
 *   <li>Server and container runtimes use file logging.</li>
 *   <li>Consumer applications do not need logging destination properties.</li>
 * </ol>
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String CATALINA_BASE = "catalina.base";
    static final String LOG_PATH = "LOG_PATH";
    static final String LOG_FILE_ENABLED = "LOG_FILE_ENABLED";
    static final String LOG_CONSOLE_ENABLED = "LOG_CONSOLE_ENABLED";
    static final String RESOLVED_APP_ENV = "IDP_LOG_APP_ENV";
    static final String APP_VERSION = "APP_VERSION";
    static final String BUILD_INFO_LOCATION = "META-INF/build-info.properties";
    static final String BUILD_VERSION_KEY = "build.version";

    static final String SPRING_APPLICATION_NAME = "spring.application.name";
    static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
    static final String SPRING_PROFILES_ACTIVE_ENV = "SPRING_PROFILES_ACTIVE";

    static final String SPRING_BOOT_TEST_BOOTSTRAPPER =
            "org.springframework.boot.test.context.SpringBootTestContextBootstrapper";
    static final String WEBMVC_TEST_BOOTSTRAPPER =
            "org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTestContextBootstrapper";
    static final String JSON_TEST_BOOTSTRAPPER =
            "org.springframework.boot.test.autoconfigure.json.JsonTestContextBootstrapper";
    static final String JPA_TEST_BOOTSTRAPPER =
            "org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTestContextBootstrapper";

    static final String SUREFIRE_TEST_CLASSPATH = "surefire.test.class.path";
    static final String FAILSAFE_TEST_CLASSPATH = "failsafe.test.class.path";

    private static boolean isConsoleOnlyRuntime(ConfigurableEnvironment environment, String appEnv) {
        if (isSpringTestBootstrap(environment) || isGenericJvmTestRuntime()) {
            return true;
        }
        return !isTomcatRuntime(environment) && isStandaloneDeveloperRuntime(appEnv);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static boolean isTomcatRuntime(ConfigurableEnvironment environment) {
        return StringUtils.hasText(firstNonBlank(
                System.getProperty(CATALINA_BASE),
                environment.getProperty(CATALINA_BASE)
        ));
    }

    private static void setFileLogging(String logPath, boolean keepConsoleEnabled) {
        System.setProperty(LOG_PATH, logPath);
        System.setProperty(LOG_FILE_ENABLED, Boolean.TRUE.toString());
        System.setProperty(LOG_CONSOLE_ENABLED, Boolean.toString(keepConsoleEnabled));
    }

    private static void setConsoleOnly() {
        System.setProperty(LOG_FILE_ENABLED, Boolean.FALSE.toString());
        System.setProperty(LOG_CONSOLE_ENABLED, Boolean.TRUE.toString());
        System.clearProperty(LOG_PATH);
    }

    private static boolean isStandaloneDeveloperRuntime(String appEnv) {
        return isLocalLikeProfile(appEnv);
    }

    private static String resolveTomcatLogPath(ConfigurableEnvironment environment) {
        String catalinaBase = firstNonBlank(
                System.getProperty(CATALINA_BASE),
                environment.getProperty(CATALINA_BASE)
        );
        if (!StringUtils.hasText(catalinaBase)) {
            throw new IllegalStateException("Tomcat runtime detected but catalina.base is not available.");
        }
        return Path.of(catalinaBase, "logs").toString();
    }

    private static boolean shouldKeepConsoleEnabled(boolean tomcatRuntime, String appEnv) {
        return !tomcatRuntime && isLocalLikeProfile(appEnv);
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return StringUtils.hasText(value)
                && StringUtils.hasText(needle)
                && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static String resolveStandaloneLogPath(String appName, String appEnv) {
        return Path.of(".", "logs", appName, sanitizeSegment(appEnv)).toString();
    }

    private static String resolveAppEnv(ConfigurableEnvironment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles != null && activeProfiles.length > 0) {
            Set<String> orderedProfiles = Arrays.stream(activeProfiles)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(profile -> profile.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!orderedProfiles.isEmpty()) {
                return String.join(",", orderedProfiles);
            }
        }

        String rawProfiles = firstNonBlank(
                System.getProperty(SPRING_PROFILES_ACTIVE),
                System.getenv(SPRING_PROFILES_ACTIVE_ENV),
                environment.getProperty(SPRING_PROFILES_ACTIVE)
        );

        if (StringUtils.hasText(rawProfiles)) {
            return Arrays.stream(rawProfiles.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(profile -> profile.toLowerCase(Locale.ROOT))
                    .distinct()
                    .collect(Collectors.joining(","));
        }

        return "default";
    }

    private static String resolveAppVersion() {
        String explicit = firstNonBlank(System.getProperty(APP_VERSION), System.getenv(APP_VERSION));
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = LoggingEnvironmentPostProcessor.class.getClassLoader();
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(BUILD_INFO_LOCATION)) {
            if (inputStream == null) {
                return "unknown-version";
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return firstNonBlank(properties.getProperty(BUILD_VERSION_KEY), "unknown-version");
        } catch (IOException ex) {
            return "unknown-version";
        }
    }

    private static boolean isLocalLikeProfile(String appEnv) {
        if (!StringUtils.hasText(appEnv)) {
            return false;
        }

        return Arrays.stream(appEnv.split(","))
                .map(String::trim)
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(profile -> profile.equals("local") || profile.startsWith("local-") || profile.startsWith("dev"));
    }

    private static boolean isSpringTestBootstrap(ConfigurableEnvironment environment) {
        return isTrue(environment.getProperty(SPRING_BOOT_TEST_BOOTSTRAPPER))
                || isTrue(environment.getProperty(WEBMVC_TEST_BOOTSTRAPPER))
                || isTrue(environment.getProperty(JSON_TEST_BOOTSTRAPPER))
                || isTrue(environment.getProperty(JPA_TEST_BOOTSTRAPPER));
    }

    private static boolean isGenericJvmTestRuntime() {
        return hasAnyText(
                System.getProperty(SUREFIRE_TEST_CLASSPATH),
                System.getProperty(FAILSAFE_TEST_CLASSPATH)
        );
    }

    private static String sanitizeSegment(String value) {
        String candidate = StringUtils.hasText(value) ? value : "default";
        return candidate.replace('\\', '-')
                .replace('/', '-')
                .replace(':', '-')
                .replace(' ', '-')
                .replace(',', '_');
    }

    private static boolean hasAnyText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value);
    }

    private static void ensureWritableDirectory(String directory, String reason) {
        Path path = Path.of(directory);
        try {
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new IllegalStateException(reason + ": [" + directory + "].", ex);
        }

        if (!Files.isDirectory(path) || !Files.isWritable(path)) {
            throw new IllegalStateException(reason + ": [" + directory + "]. "
                    + "Ensure the application runtime user has write access.");
        }
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String appName = sanitizeSegment(firstNonBlank(environment.getProperty(SPRING_APPLICATION_NAME), "idp-app"));
        String appEnv = resolveAppEnv(environment);

        System.setProperty(RESOLVED_APP_ENV, appEnv);
        System.setProperty(APP_VERSION, resolveAppVersion());

        if (isConsoleOnlyRuntime(environment, appEnv)) {
            setConsoleOnly();
            return;
        }

        boolean tomcatRuntime = isTomcatRuntime(environment);
        String resolvedPath = tomcatRuntime
                ? resolveTomcatLogPath(environment)
                : resolveStandaloneLogPath(appName, appEnv);

        ensureWritableDirectory(
                resolvedPath,
                "Logging initialization failed because the resolved log directory could not be prepared"
        );

        setFileLogging(resolvedPath, shouldKeepConsoleEnabled(tomcatRuntime, appEnv));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
