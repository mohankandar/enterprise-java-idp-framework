package io.github.mohankandar.idp.platform.logging;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void usesInboundCorrelationIdAndRestoresPreviousMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HDR, "incoming-corr-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, "previous-value");

        filter.doFilter(request, response, (req, res) ->
                assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isEqualTo("incoming-corr-id")
        );

        assertThat(response.getHeader(CorrelationIdFilter.HDR)).isEqualTo("incoming-corr-id");
        assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isEqualTo("previous-value");
    }

    @Test
    void generatesCorrelationIdWhenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) ->
                assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isNotBlank()
        );

        assertThat(response.getHeader(CorrelationIdFilter.HDR)).isNotBlank();
        assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isNull();
    }
}
