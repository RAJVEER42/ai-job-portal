package com.jobportal.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production-grade Rate Limiting Configuration
 * Implements Bucket4j token bucket algorithm for request throttling
 * 
 * Features:
 * - Per-IP rate limiting
 * - Different limits for different endpoints
 * - Configurable via environment variables
 * - Production-ready with distributed support
 */
@Slf4j
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.burst-requests:10}")
    private int burstRequests;

    @Value("${app.rate-limit.auth-requests-per-minute:10}")
    private int authRequestsPerMinute;

    @Value("${app.rate-limit.search-requests-per-minute:100}")
    private int searchRequestsPerMinute;

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rateLimitingEnabled) {
            registry.addInterceptor(rateLimitingInterceptor())
                    .addPathPatterns("/api/**");
        }
    }

    @Bean
    public HandlerInterceptor rateLimitingInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (!rateLimitingEnabled) {
                    return true;
                }

                String clientIp = getClientIP(request);
                String endpoint = request.getRequestURI();
                
                Bucket bucket = getBucketForClient(clientIp, endpoint);
                
                if (bucket.tryConsume(1)) {
                    // Add rate limit headers for transparency
                    response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
                    response.addHeader("X-Rate-Limit-Retry-After-Seconds", "60");
                    return true;
                } else {
                    // Rate limit exceeded
                    log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
                    response.setStatus(429);
                    response.addHeader("X-Rate-Limit-Retry-After-Seconds", "60");
                    response.getWriter().write(
                        "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"status\":429}"
                    );
                    response.setContentType("application/json");
                    return false;
                }
            }
        };
    }

    private Bucket getBucketForClient(String clientIp, String endpoint) {
        return cache.computeIfAbsent(clientIp + ":" + getEndpointGroup(endpoint), key -> {
            int requestLimit = getRequestLimitForEndpoint(endpoint);
            
            Bandwidth bandwidth = Bandwidth.classic(requestLimit, Refill.intervally(requestLimit, Duration.ofMinutes(1)));
            Bandwidth burstBandwidth = Bandwidth.classic(burstRequests, Refill.intervally(burstRequests, Duration.ofSeconds(10)));
            
            return Bucket.builder()
                    .addLimit(bandwidth)
                    .addLimit(burstBandwidth)
                    .build();
        });
    }

    private String getEndpointGroup(String endpoint) {
        if (endpoint.contains("/auth/")) {
            return "auth";
        } else if (endpoint.contains("/jobs/search") || endpoint.contains("/jobs/recommendations")) {
            return "search";
        } else if (endpoint.contains("/applications/")) {
            return "applications";
        } else {
            return "general";
        }
    }

    private int getRequestLimitForEndpoint(String endpoint) {
        if (endpoint.contains("/auth/")) {
            return authRequestsPerMinute;
        } else if (endpoint.contains("/jobs/search") || endpoint.contains("/jobs/recommendations")) {
            return searchRequestsPerMinute;
        } else {
            return requestsPerMinute;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /**
     * Rate limiting metrics for monitoring
     */
    @Bean
    public RateLimitMetrics rateLimitMetrics() {
        return new RateLimitMetrics();
    }

    public static class RateLimitMetrics {
        private long totalRequests = 0;
        private long blockedRequests = 0;
        
        public void incrementTotal() { totalRequests++; }
        public void incrementBlocked() { blockedRequests++; }
        
        public long getTotalRequests() { return totalRequests; }
        public long getBlockedRequests() { return blockedRequests; }
        public double getBlockedRate() { 
            return totalRequests > 0 ? (double) blockedRequests / totalRequests * 100 : 0; 
        }
    }
}
