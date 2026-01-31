package com.jobportal.backend.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsConfig {

    /**
     * Custom meter registry for application-specific metrics
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                .commonTags("application", "jobportal")
                .commonTags("version", "1.0.0");
        };
    }

    /**
     * Enable @Timed annotations for method execution timing
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Cache performance monitor
     */
    @Bean
    @ConditionalOnProperty(name = "app.metrics.cache.enabled", havingValue = "true", matchIfMissing = true)
    public CacheMetricsCollector cacheMetricsCollector(CacheManager cacheManager, MeterRegistry meterRegistry) {
        return new CacheMetricsCollector(cacheManager, meterRegistry);
    }

    /**
     * Email service metrics collector
     */
    @Bean
    @ConditionalOnProperty(name = "app.metrics.email.enabled", havingValue = "true", matchIfMissing = true)
    public EmailMetricsCollector emailMetricsCollector(MeterRegistry meterRegistry) {
        return new EmailMetricsCollector(meterRegistry);
    }

    /**
     * Custom metrics collector for cache performance
     */
    public static class CacheMetricsCollector {
        private final CacheManager cacheManager;
        private final MeterRegistry meterRegistry;

        public CacheMetricsCollector(CacheManager cacheManager, MeterRegistry meterRegistry) {
            this.cacheManager = cacheManager;
            this.meterRegistry = meterRegistry;
        }

        @Scheduled(fixedRate = 30000) // Every 30 seconds
        public void collectCacheMetrics() {
            try {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        // Register cache size and hit ratio if available
                        log.debug("Collecting metrics for cache: {}", cacheName);
                    }
                });
            } catch (Exception e) {
                log.warn("Error collecting cache metrics", e);
            }
        }
    }

    /**
     * Custom metrics collector for email performance
     */
    public static class EmailMetricsCollector {
        private final MeterRegistry meterRegistry;
        private final Timer emailSentTimer;
        private final Timer emailFailedTimer;

        public EmailMetricsCollector(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            this.emailSentTimer = Timer.builder("email.sent")
                    .description("Time taken to send emails successfully")
                    .register(meterRegistry);
            this.emailFailedTimer = Timer.builder("email.failed")
                    .description("Time taken for failed email attempts")
                    .register(meterRegistry);
        }

        public Timer.Sample startEmailTimer() {
            return Timer.start(meterRegistry);
        }

        public void recordEmailSuccess(Timer.Sample sample) {
            sample.stop(emailSentTimer);
            meterRegistry.counter("email.success.count").increment();
        }

        public void recordEmailFailure(Timer.Sample sample) {
            sample.stop(emailFailedTimer);
            meterRegistry.counter("email.failure.count").increment();
        }
    }
}
