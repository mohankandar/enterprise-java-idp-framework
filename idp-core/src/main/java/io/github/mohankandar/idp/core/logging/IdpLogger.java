package io.github.mohankandar.idp.core.logging;

import org.slf4j.Logger;

/**
 * IDP logging façade.
 *
 * <p>Consumer applications should depend on this abstraction (not raw SLF4J) so the framework
 * can evolve structured logging, masking, audit tagging, sampling, and tracing integrations
 * behind a stable API.
 */
public final class IdpLogger {

    private final Logger delegate;

    IdpLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public void debug(String message, Object... args) {
        delegate.debug(message, args);
    }

    public void info(String message, Object... args) {
        delegate.info(message, args);
    }

    public void warn(String message, Object... args) {
        delegate.warn(message, args);
    }

    public void error(String message, Object... args) {
        delegate.error(message, args);
    }

    public void error(String message, Throwable t) {
        delegate.error(message, t);
    }

    public void error(String message, Throwable t, Object... args) {
        // SLF4J supports (String, Object...) with Throwable as last arg; this overload keeps the callsite explicit.
        Object[] merged = new Object[args.length + 1];
        System.arraycopy(args, 0, merged, 0, args.length);
        merged[args.length] = t;
        delegate.error(message, merged);
    }
}
