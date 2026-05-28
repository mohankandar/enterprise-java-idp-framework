package io.github.mohankandar.idp.core.logging;

import org.slf4j.LoggerFactory;

/**
 * Factory for {@link IdpLogger}.
 */
public final class IdpLoggerFactory {

    private IdpLoggerFactory() {
    }

    public static IdpLogger getLogger(Class<?> type) {
        return new IdpLogger(LoggerFactory.getLogger(type));
    }
}
