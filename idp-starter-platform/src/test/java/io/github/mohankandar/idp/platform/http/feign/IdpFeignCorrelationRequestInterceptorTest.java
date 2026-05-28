package io.github.mohankandar.idp.platform.http.feign;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class IdpFeignCorrelationRequestInterceptorTest {

    private final IdpFeignCorrelationRequestInterceptor interceptor = new IdpFeignCorrelationRequestInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void addsCorrelationIdFromMdcWhenMissing() {
        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, "mdc-correlation-id");
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).containsKey(IdpLoggingConstants.CORRELATION_HEADER);
        assertThat(template.headers().get(IdpLoggingConstants.CORRELATION_HEADER)).containsExactly("mdc-correlation-id");
    }

    @Test
    void keepsExistingCorrelationHeaderUntouched() {
        RequestTemplate template = new RequestTemplate();
        template.header(IdpLoggingConstants.CORRELATION_HEADER, "already-set");

        interceptor.apply(template);

        assertThat(template.headers().get(IdpLoggingConstants.CORRELATION_HEADER)).containsExactly("already-set");
    }
}
