package io.github.mohankandar.idp.core.util;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Simple redactors for logs. Keep minimal; platform adds route-level masking as needed.
 */
public final class MaskingUtil {
    private MaskingUtil() {}

    // Basic patterns (conservative). Adjust as needed in platform filter.
    private static final Pattern SSN = Pattern.compile("\\b(\\d{3})[- ]?(\\d{2})[- ]?(\\d{4})\\b");
    private static final Pattern CREDIT_CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");
    private static final Pattern BEARER = Pattern.compile("(?i)\\b(bearer)\\s+([a-z0-9._\\-]+)\\b");
    private static final Pattern APP_TOKEN = Pattern.compile("(?i)(idp\\.app\\.token\"?\\s*[:=]\\s*\")([^\"]+)(\")");

    public static String maskAll(String input) {
        if (input == null || input.isBlank()) return input;
        String s = input;
        s = SSN.matcher(s).replaceAll("***-**-$3");
        s = CREDIT_CARD.matcher(s).replaceAll("************MASKED");
        s = BEARER.matcher(s).replaceAll("$1 *****");
        s = APP_TOKEN.matcher(s).replaceAll("$1*****$3");
        return s;
    }

    /** Keep last 4 chars, mask the rest. */
    public static String last4(String value) {
        Objects.requireNonNull(value, "value");
        if (value.length() <= 4) return "****";
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
