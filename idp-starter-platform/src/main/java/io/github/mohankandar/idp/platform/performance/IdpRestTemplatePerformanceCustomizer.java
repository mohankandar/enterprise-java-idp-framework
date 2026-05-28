package io.github.mohankandar.idp.platform.performance;

import io.github.mohankandar.idp.core.logging.IdpLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mohankandar.idp.core.logging.IdpLoggerFactory;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IdpRestTemplatePerformanceCustomizer implements RestTemplateCustomizer {

    private static final IdpLogger log = IdpLoggerFactory.getLogger(IdpRestTemplatePerformanceCustomizer.class);

    private final IdpPerformanceProperties properties;
    private final IdpPerformanceMetrics metrics;

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed collaborators are injected and intentionally retained for the bean lifecycle."
    )
    public IdpRestTemplatePerformanceCustomizer(IdpPerformanceProperties properties, IdpPerformanceMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        List<org.springframework.http.client.ClientHttpRequestInterceptor> interceptors =
            new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add((request, body, execution) -> {
            if (!properties.isEnabled()) {
                return execution.execute(request, body);
            }

            long start = System.nanoTime();
            IOException failure = null;

            try {
                return execution.execute(request, body);
            } catch (IOException ex) {
                failure = ex;
                throw ex;
            } finally {
                long durationNs = System.nanoTime() - start;
                long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNs);
                String host = request.getURI().getHost() != null ? request.getURI().getHost() : "unknown";
                String method = request.getMethod().name();
                String target = method + " " + request.getURI();

                if (properties.getMetrics().isEnabled()) {
                    metrics.record("http", host, method, durationNs, failure == null);
                }

                IdpPerformanceLogger.logIfBreached(
                    log,
                    "http",
                    target,
                    durationMs,
                    properties.getThresholds().getHttpMs(),
                    failure == null ? "success" : "error"
                );
            }
        });
        restTemplate.setInterceptors(interceptors);
    }
}
