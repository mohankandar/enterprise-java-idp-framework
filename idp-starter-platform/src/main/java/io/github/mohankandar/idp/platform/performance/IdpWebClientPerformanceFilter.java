package io.github.mohankandar.idp.platform.performance;

import io.github.mohankandar.idp.core.logging.IdpLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mohankandar.idp.core.logging.IdpLoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public class IdpWebClientPerformanceFilter implements ExchangeFilterFunction {

    private static final IdpLogger log = IdpLoggerFactory.getLogger(IdpWebClientPerformanceFilter.class);

    private final IdpPerformanceProperties properties;
    private final IdpPerformanceMetrics metrics;

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed collaborators are injected and intentionally retained for the bean lifecycle."
    )
    public IdpWebClientPerformanceFilter(IdpPerformanceProperties properties, IdpPerformanceMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public Mono<ClientResponse> filter(org.springframework.web.reactive.function.client.ClientRequest request, org.springframework.web.reactive.function.client.ExchangeFunction next) {
        if (!properties.isEnabled()) {
            return next.exchange(request);
        }

        long start = System.nanoTime();
        String host = request.url().getHost() != null ? request.url().getHost() : "unknown";
        String method = request.method().name();
        String target = method + " " + request.url();

        return next.exchange(request)
            .doOnSuccess(response -> record(host, method, target, start, true))
            .doOnError(error -> record(host, method, target, start, false));
    }

    private void record(String host, String method, String target, long start, boolean success) {
        long durationNs = System.nanoTime() - start;
        long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNs);

        if (properties.getMetrics().isEnabled()) {
            metrics.record("http", host, method, durationNs, success);
        }

        IdpPerformanceLogger.logIfBreached(
            log,
            "http",
            target,
            durationMs,
            properties.getThresholds().getHttpMs(),
            success ? "success" : "error"
        );
    }
}
