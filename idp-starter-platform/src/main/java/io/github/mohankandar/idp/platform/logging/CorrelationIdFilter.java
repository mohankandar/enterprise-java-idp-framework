package io.github.mohankandar.idp.platform.logging;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String HDR = IdpLoggingConstants.CORRELATION_HEADER;
    public static final String MDC_CORRELATION_ID = IdpLoggingConstants.MDC_CORRELATION_ID;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {

        String cid = req.getHeader(HDR);
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        res.setHeader(HDR, cid);

        final String prevCid = MDC.get(MDC_CORRELATION_ID);
        MDC.put(MDC_CORRELATION_ID, cid);

        try {
            chain.doFilter(req, res);
        } finally {
            restore(MDC_CORRELATION_ID, prevCid);
        }
    }

    private static void restore(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previousValue);
        }
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
