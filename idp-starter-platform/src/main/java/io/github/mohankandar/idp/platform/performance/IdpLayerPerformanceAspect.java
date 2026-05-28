package io.github.mohankandar.idp.platform.performance;

import io.github.mohankandar.idp.core.logging.IdpLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.mohankandar.idp.core.logging.IdpLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class IdpLayerPerformanceAspect {

    private static final IdpLogger log = IdpLoggerFactory.getLogger(IdpLayerPerformanceAspect.class);

    private final IdpPerformanceProperties properties;
    private final IdpPerformanceMetrics metrics;

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed collaborators are injected and intentionally retained for the bean lifecycle."
    )
    public IdpLayerPerformanceAspect(IdpPerformanceProperties properties, IdpPerformanceMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Around(
        "within(@org.springframework.web.bind.annotation.RestController *) || " +
            "within(@org.springframework.stereotype.Controller *)"
    )
    public Object measureControllers(ProceedingJoinPoint pjp) throws Throwable {
        return measure(pjp, "controller", properties.getThresholds().getControllerMs());
    }

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object measureServices(ProceedingJoinPoint pjp) throws Throwable {
        return measure(pjp, "service", properties.getThresholds().getServiceMs());
    }

    private Object measure(ProceedingJoinPoint pjp, String type, long thresholdMs) throws Throwable {
        if (!properties.isEnabled()) {
            return pjp.proceed();
        }

        long start = System.nanoTime();
        Throwable failure = null;

        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationNs = System.nanoTime() - start;
            long durationMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(durationNs);
            String className = pjp.getSignature().getDeclaringTypeName();
            String methodName = pjp.getSignature().getName();

            if (properties.getMetrics().isEnabled()) {
                metrics.record(type, className, methodName, durationNs, failure == null);
            }

            IdpPerformanceLogger.logIfBreached(
                log,
                type,
                pjp.getSignature().toShortString(),
                durationMs,
                thresholdMs,
                failure == null ? "success" : "error"
            );
        }
    }
}
