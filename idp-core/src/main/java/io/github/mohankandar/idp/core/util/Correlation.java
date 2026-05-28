package io.github.mohankandar.idp.core.util;

import java.util.UUID;

/** Correlation helpers (platform will set MDC, but this stays logger-agnostic). */
public final class Correlation {
    private Correlation() {}

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
