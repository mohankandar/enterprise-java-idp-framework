package io.github.mohankandar.idp.platform.performance;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

public class IdpPerformanceMetrics {

    private final MeterRegistry meterRegistry;

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "MeterRegistry is a Spring-managed infrastructure dependency intentionally retained by this singleton bean."
    )
    public IdpPerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void record(String type, String className, String method, long durationNs, boolean success) {
        Timer.builder("idp.perf." + type)
            .description("IDP performance timings for " + type)
            .tag("class", className)
            .tag("method", method)
            .tag("outcome", success ? "success" : "error")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(meterRegistry)
            .record(durationNs, TimeUnit.NANOSECONDS);
    }
}
