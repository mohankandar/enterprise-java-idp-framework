package io.github.mohankandar.idp.platform.http.feign;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class IdpFeignCorrelationRequestInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdpFeignCorrelationRequestInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        if (hasHeader(template, IdpLoggingConstants.CORRELATION_HEADER)) {
            return;
        }

        String correlationId = MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            log.warn("Missing correlationId in MDC before Feign outbound call; generated fallback correlationId={}", correlationId);
        }

        template.header(IdpLoggingConstants.CORRELATION_HEADER, correlationId);
    }

    private static boolean hasHeader(RequestTemplate template, String header) {
        return template.headers().keySet().stream().anyMatch(h -> h.equalsIgnoreCase(header));
    }
}
