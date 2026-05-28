package io.github.mohankandar.idp.data.performance;

import io.github.mohankandar.idp.core.logging.IdpLogger;
import io.github.mohankandar.idp.core.logging.IdpLoggerFactory;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceLogger;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceMetrics;
import io.github.mohankandar.idp.platform.performance.IdpPerformanceProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IdpDataSourcePerformanceBeanPostProcessor implements BeanPostProcessor {

    private static final IdpLogger log = IdpLoggerFactory.getLogger(IdpDataSourcePerformanceBeanPostProcessor.class);

    private final IdpPerformanceProperties properties;
    private final IdpPerformanceMetrics metrics;

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed configuration and metrics collaborators are intentionally stored for bean post-processing."
    )
    public IdpDataSourcePerformanceBeanPostProcessor(IdpPerformanceProperties properties, IdpPerformanceMetrics metrics) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof DataSource dataSource)) {
            return bean;
        }
        if (!properties.isEnabled() || !properties.isDbEnabled() || bean instanceof ProxyDataSource) {
            return bean;
        }

        return ProxyDataSourceBuilder
            .create(dataSource)
            .name(beanName)
            .listener(new PerfQueryExecutionListener(beanName, properties, metrics))
            .build();
    }

    private static final class PerfQueryExecutionListener implements QueryExecutionListener {

        private final String dataSourceName;
        private final IdpPerformanceProperties properties;
        private final IdpPerformanceMetrics metrics;

        @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring-managed configuration and metrics collaborators are intentionally retained by the query listener."
        )
        private PerfQueryExecutionListener(String dataSourceName,
                                           IdpPerformanceProperties properties,
                                           IdpPerformanceMetrics metrics) {
            this.dataSourceName = dataSourceName;
            this.properties = properties;
            this.metrics = metrics;
        }

        @Override
        public void beforeQuery(net.ttddyy.dsproxy.ExecutionInfo execInfo,
                                List<net.ttddyy.dsproxy.QueryInfo> queryInfoList) {
            // no-op
        }

        @Override
        public void afterQuery(net.ttddyy.dsproxy.ExecutionInfo execInfo,
                               List<net.ttddyy.dsproxy.QueryInfo> queryInfoList) {
            long durationNs = TimeUnit.MILLISECONDS.toNanos(execInfo.getElapsedTime());
            long durationMs = execInfo.getElapsedTime();
            String operation = queryInfoList.stream()
                .filter(queryInfo -> queryInfo.getQuery() != null && !queryInfo.getQuery().isBlank())
                .map(queryInfo -> summarize(queryInfo.getQuery()))
                .findFirst()
                .orElse("unknown");

            if (properties.getMetrics().isEnabled()) {
                metrics.record("db", dataSourceName, operation, durationNs, true);
            }

            IdpPerformanceLogger.logIfBreached(
                log,
                "db",
                dataSourceName + " " + operation,
                durationMs,
                properties.getThresholds().getDbMs(),
                "success"
            );
        }

        private static String summarize(String sql) {
            String normalized = sql.replaceAll("\\s+", " ").trim();
            if (normalized.isEmpty()) {
                return "unknown";
            }
            if (normalized.length() > 160) {
                return normalized.substring(0, 160) + "...";
            }
            return normalized;
        }
    }
}
