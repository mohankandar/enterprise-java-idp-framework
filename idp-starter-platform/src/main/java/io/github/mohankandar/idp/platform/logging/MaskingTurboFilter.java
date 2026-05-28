package io.github.mohankandar.idp.platform.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import io.github.mohankandar.idp.core.util.MaskingUtil;
import org.slf4j.Marker;

/**
 * Minimal, safe masker: scrubs any String arguments before they reach appenders.
 * We DO NOT rewrite or re-emit events (avoids Logback SPI churn).
 */
public class MaskingTurboFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker,
                              Logger logger,
                              Level level,
                              String message,
                              Object[] params,
                              Throwable t) {
        try {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    Object p = params[i];
                    if (p instanceof CharSequence cs) {
                        params[i] = MaskingUtil.maskAll(cs.toString());
                    }
                }
            }
        } catch (Throwable ignored) {
            // never break logging
        }
        return FilterReply.NEUTRAL;
    }
}
