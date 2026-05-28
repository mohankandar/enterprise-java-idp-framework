package io.github.mohankandar.idp.platform.async;

import io.github.mohankandar.idp.core.logging.IdpLoggingConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void propagatesCapturedMdcIntoDecoratedTask() {
        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, "corr-async");
        MDC.put(IdpLoggingConstants.MDC_USER, "api-user");

        AtomicReference<String> seenCorrelationId = new AtomicReference<>();
        AtomicReference<String> seenUser = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> {
            seenCorrelationId.set(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID));
            seenUser.set(MDC.get(IdpLoggingConstants.MDC_USER));
        });

        MDC.clear();
        decorated.run();

        assertThat(seenCorrelationId.get()).isEqualTo("corr-async");
        assertThat(seenUser.get()).isEqualTo("api-user");
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    @Test
    void restoresPreviousExecutorThreadContextAfterTaskCompletes() {
        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, "request-corr");
        Runnable decorated = decorator.decorate(() ->
                assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isEqualTo("request-corr"));

        MDC.put(IdpLoggingConstants.MDC_CORRELATION_ID, "executor-corr");
        MDC.put(IdpLoggingConstants.MDC_USER, "executor-user");

        decorated.run();

        assertThat(MDC.get(IdpLoggingConstants.MDC_CORRELATION_ID)).isEqualTo("executor-corr");
        assertThat(MDC.get(IdpLoggingConstants.MDC_USER)).isEqualTo("executor-user");
    }
}
