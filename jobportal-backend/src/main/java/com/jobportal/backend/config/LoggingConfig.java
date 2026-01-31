package com.jobportal.backend.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Enterprise-grade Logging Configuration
 * 
 * Features:
 * - Structured JSON logging for production
 * - Request/Response logging with correlation IDs
 * - Performance monitoring
 * - File rotation and retention
 * - Security audit logging
 * - Configurable log levels per environment
 */
@Slf4j
@Configuration
public class LoggingConfig {

    @Value("${app.logging.level:INFO}")
    private String logLevel;

    @Value("${app.logging.file.enabled:false}")
    private boolean fileLoggingEnabled;

    @Value("${app.logging.file.path:/var/log/jobportal}")
    private String logFilePath;

    @Value("${app.logging.request.enabled:true}")
    private boolean requestLoggingEnabled;

    @Value("${app.logging.performance.enabled:true}")
    private boolean performanceLoggingEnabled;

    @Value("${app.logging.security.enabled:true}")
    private boolean securityLoggingEnabled;

    /**
     * Request/Response logging filter
     */
    @Bean
    @Profile("!test")
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    /**
     * Security audit logger
     */
    @Bean
    public SecurityAuditLogger securityAuditLogger() {
        return new SecurityAuditLogger();
    }

    /**
     * Performance logger
     */
    @Bean
    public PerformanceLogger performanceLogger() {
        return new PerformanceLogger();
    }

    public class RequestLoggingFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            if (!requestLoggingEnabled) {
                filterChain.doFilter(request, response);
                return;
            }

            // Generate correlation ID
            String correlationId = UUID.randomUUID().toString();
            request.setAttribute("correlationId", correlationId);
            response.setHeader("X-Correlation-ID", correlationId);

            long startTime = System.currentTimeMillis();

            try {
                // Log request
                logRequest(request, correlationId);
                
                filterChain.doFilter(request, response);
                
            } finally {
                // Log response
                long duration = System.currentTimeMillis() - startTime;
                logResponse(request, response, correlationId, duration);
            }
        }

        private void logRequest(HttpServletRequest request, String correlationId) {
            log.info("REQUEST [{}] {} {} from {} - User-Agent: {}", 
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                getClientIP(request),
                request.getHeader("User-Agent"));
        }

        private void logResponse(HttpServletRequest request, HttpServletResponse response, 
                               String correlationId, long duration) {
            log.info("RESPONSE [{}] {} {} - Status: {} - Duration: {}ms", 
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

            // Log slow requests
            if (performanceLoggingEnabled && duration > 2000) {
                log.warn("SLOW_REQUEST [{}] {} {} took {}ms", 
                    correlationId,
                    request.getMethod(),
                    request.getRequestURI(),
                    duration);
            }
        }

        private String getClientIP(HttpServletRequest request) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0].trim();
        }
    }

    /**
     * Security audit logging
     */
    public static class SecurityAuditLogger {
        private final Logger auditLogger = (Logger) LoggerFactory.getLogger("SECURITY_AUDIT");

        public void logLoginAttempt(String username, String ip, boolean success) {
            if (success) {
                auditLogger.info("LOGIN_SUCCESS - User: {} from IP: {}", username, ip);
            } else {
                auditLogger.warn("LOGIN_FAILED - User: {} from IP: {}", username, ip);
            }
        }

        public void logPasswordChange(String username, String ip) {
            auditLogger.info("PASSWORD_CHANGE - User: {} from IP: {}", username, ip);
        }

        public void logAccountCreation(String username, String ip) {
            auditLogger.info("ACCOUNT_CREATED - User: {} from IP: {}", username, ip);
        }

        public void logPermissionDenied(String username, String resource, String ip) {
            auditLogger.warn("PERMISSION_DENIED - User: {} tried to access {} from IP: {}", 
                username, resource, ip);
        }

        public void logSuspiciousActivity(String description, String ip) {
            auditLogger.error("SUSPICIOUS_ACTIVITY - {} from IP: {}", description, ip);
        }
    }

    /**
     * Performance monitoring logger
     */
    public static class PerformanceLogger {
        private final Logger perfLogger = (Logger) LoggerFactory.getLogger("PERFORMANCE");

        public void logDatabaseQuery(String query, long duration) {
            if (duration > 1000) {
                perfLogger.warn("SLOW_DB_QUERY - Query took {}ms: {}", duration, query);
            }
        }

        public void logCacheOperation(String operation, String key, boolean hit, long duration) {
            perfLogger.debug("CACHE_{} - Key: {} - Hit: {} - Duration: {}ms", 
                operation, key, hit, duration);
        }

        public void logEmailOperation(String operation, String recipient, boolean success, long duration) {
            if (success) {
                perfLogger.info("EMAIL_{} - To: {} - Duration: {}ms", operation, recipient, duration);
            } else {
                perfLogger.error("EMAIL_{}_FAILED - To: {} - Duration: {}ms", operation, recipient, duration);
            }
        }

        public void logApiCall(String service, String endpoint, int statusCode, long duration) {
            if (statusCode >= 400) {
                perfLogger.error("EXTERNAL_API_ERROR - Service: {} Endpoint: {} Status: {} Duration: {}ms",
                    service, endpoint, statusCode, duration);
            } else if (duration > 5000) {
                perfLogger.warn("SLOW_EXTERNAL_API - Service: {} Endpoint: {} Duration: {}ms",
                    service, endpoint, duration);
            }
        }
    }

    /**
     * Structured logging for production environments
     */
    @Configuration
    @Profile("production")
    static class ProductionLoggingConfig {

        public ProductionLoggingConfig() {
            configureProductionLogging();
        }

        private void configureProductionLogging() {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            // Configure JSON pattern for structured logging
            String jsonPattern = "{"
                + "\"timestamp\":\"%d{yyyy-MM-dd HH:mm:ss.SSS}\","
                + "\"level\":\"%level\","
                + "\"thread\":\"%thread\","
                + "\"logger\":\"%logger{36}\","
                + "\"message\":\"%msg\","
                + "\"exception\":\"%ex\""
                + "}%n";

            // Console appender with JSON format
            ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
                new ConsoleAppender<>();
            consoleAppender.setContext(context);
            consoleAppender.setName("CONSOLE");

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern(jsonPattern);
            encoder.start();

            consoleAppender.setEncoder(encoder);
            consoleAppender.start();

            // File appender with rolling policy
            RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
                new RollingFileAppender<>();
            fileAppender.setContext(context);
            fileAppender.setName("FILE");
            fileAppender.setFile("/var/log/jobportal/application.log");

            TimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = 
                new TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setParent(fileAppender);
            rollingPolicy.setFileNamePattern("/var/log/jobportal/application.%d{yyyy-MM-dd}.%i.log.gz");
            rollingPolicy.setMaxHistory(30); // Keep 30 days
            rollingPolicy.start();

            PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
            fileEncoder.setContext(context);
            fileEncoder.setPattern(jsonPattern);
            fileEncoder.start();

            fileAppender.setRollingPolicy(rollingPolicy);
            fileAppender.setEncoder(fileEncoder);
            fileAppender.start();

            // Configure root logger
            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(consoleAppender);
            rootLogger.addAppender(fileAppender);
            rootLogger.setLevel(Level.INFO);
        }
    }
}
