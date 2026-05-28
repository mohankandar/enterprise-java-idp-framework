package io.github.mohankandar.idp.platform.performance;

import io.github.mohankandar.idp.core.logging.IdpLogger;

public final class IdpPerformanceLogger {

    private IdpPerformanceLogger() {
    }

    public static void logIfBreached(
        IdpLogger log,
        String type,
        String target,
        long durationMs,
        long thresholdMs,
        String outcome) {

        if (durationMs < thresholdMs) {
            return;
        }

        long exceededMs = durationMs - thresholdMs;

        log.warn(
            "[IDP-PERF] type={} target={} durationMs={} thresholdMs={} exceededMs={} outcome={}",
            type,
            target,
            durationMs,
            thresholdMs,
            exceededMs,
            outcome
        );
    }
}
