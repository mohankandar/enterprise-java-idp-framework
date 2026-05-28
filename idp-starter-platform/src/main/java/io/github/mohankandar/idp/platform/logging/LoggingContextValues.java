package io.github.mohankandar.idp.platform.logging;

import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LoggingContextValues {

    private final String appName;
    private final String appEnv;
    private final String host;
    private final String pid;
    private final String appVersion;

    private LoggingContextValues(String appName, String appEnv, String host, String pid, String appVersion) {
        this.appName = blankToNull(appName);
        this.appEnv = blankToNull(appEnv);
        this.host = blankToNull(host);
        this.pid = blankToNull(pid);
        this.appVersion = blankToNull(appVersion);
    }

    public static LoggingContextValues from(String appName, Environment environment, BuildProperties buildProperties) {
        String resolvedEnv = resolveEnv(environment);
        String resolvedHost = resolveHost();
        String resolvedPid = resolvePid();
        String resolvedVersion = resolveVersion(buildProperties);
        return new LoggingContextValues(appName, resolvedEnv, resolvedHost, resolvedPid, resolvedVersion);
    }

    public String appName() {
        return appName;
    }

    public String appEnv() {
        return appEnv;
    }

    public String host() {
        return host;
    }

    public String pid() {
        return pid;
    }

    public String appVersion() {
        return appVersion;
    }

    private static String resolveEnv(Environment environment) {
        String explicit = blankToNull(environment.getProperty("idp.logging.app-env"));
        if (explicit != null) {
            return explicit;
        }

        String[] profiles = environment.getActiveProfiles();
        if (profiles.length > 0) {
            return Arrays.stream(profiles)
                .filter(profile -> profile != null && !profile.isBlank())
                .collect(Collectors.joining(","));
        }

        return blankToNull(environment.getProperty("spring.profiles.active", "default"));
    }

    private static String resolveHost() {
        try {
            return blankToNull(InetAddress.getLocalHost().getHostName());
        } catch (Exception ex) {
            return null;
        }
    }

    private static String resolvePid() {
        try {
            return Long.toString(ProcessHandle.current().pid());
        } catch (Exception ex) {
            return null;
        }
    }

    private static String resolveVersion(BuildProperties buildProperties) {
        if (buildProperties != null) {
            return blankToNull(buildProperties.getVersion());
        }
        return blankToNull(Optional.ofNullable(LoggingContextValues.class.getPackage().getImplementationVersion())
            .orElse(null));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
