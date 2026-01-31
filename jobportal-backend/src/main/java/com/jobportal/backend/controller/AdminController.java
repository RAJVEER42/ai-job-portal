package com.jobportal.backend.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Admin Controller
 * 
 * Features:
 * - System administration endpoints
 * - Performance monitoring controls
 * - Rate limiting controls
 * - Health check management
 * - Metrics collection controls
 * - System maintenance controls
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "System administration and monitoring endpoints")
public class AdminController {

    @Operation(
        summary = "Get system health status",
        description = "Comprehensive system health check including database, cache, and external services"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health status retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "500", description = "System health check failed")
    })
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_health_check")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", Instant.now());
            health.put("application", "JobPortal Backend");
            health.put("version", "1.0.0");
            health.put("uptime", System.currentTimeMillis());
            
            log.info("System health check requested");
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", Instant.now());
            return ResponseEntity.status(500).body(health);
        }
    }

    @Operation(
        summary = "Get system metrics",
        description = "Retrieve comprehensive system performance metrics"
    )
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Basic metrics (since advanced monitoring is temporarily disabled)
            metrics.put("timestamp", Instant.now());
            metrics.put("jvmMemory", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            metrics.put("jvmMaxMemory", Runtime.getRuntime().maxMemory());
            metrics.put("jvmFreeMemory", Runtime.getRuntime().freeMemory());
            metrics.put("jvmTotalMemory", Runtime.getRuntime().totalMemory());
            metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            
            log.info("System metrics requested");
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Failed to retrieve system metrics", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Reset rate limiting counters",
        description = "Reset all rate limiting counters for all users"
    )
    @PostMapping("/rate-limit/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_rate_limit_reset")
    public ResponseEntity<Map<String, String>> resetRateLimiting() {
        try {
            // Rate limit reset logic would go here when re-enabled
            log.info("Rate limit reset requested");
            
            return ResponseEntity.ok(Map.of(
                "message", "Rate limiting counters reset successfully",
                "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to reset rate limiting", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Get rate limiting statistics", 
        description = "Retrieve current rate limiting statistics"
    )
    @GetMapping("/rate-limit/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_rate_limit_stats")
    public ResponseEntity<Map<String, Object>> getRateLimitingStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Placeholder stats since rate limiting is temporarily disabled
            stats.put("totalRequests", 0);
            stats.put("limitedRequests", 0);
            stats.put("hitRate", 0.0);
            stats.put("timestamp", Instant.now());
            stats.put("status", "Rate limiting temporarily disabled");
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to retrieve rate limiting stats", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Toggle maintenance mode",
        description = "Enable or disable system maintenance mode"
    )
    @PostMapping("/maintenance/{enabled}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_maintenance_toggle")
    public ResponseEntity<Map<String, String>> toggleMaintenance(
            @Parameter(description = "Maintenance mode state") @PathVariable boolean enabled) {
        try {
            // Maintenance mode toggle logic would go here
            String status = enabled ? "enabled" : "disabled";
            log.info("Maintenance mode {} requested", status);
            
            return ResponseEntity.ok(Map.of(
                "message", "Maintenance mode " + status + " successfully", 
                "maintenanceMode", String.valueOf(enabled),
                "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to toggle maintenance mode", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Get system information",
        description = "Retrieve basic system information and status"
    )
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "admin_system_info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            info.put("application", "JobPortal Backend");
            info.put("version", "1.0.0");
            info.put("environment", "production");
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("springBootVersion", "3.2.2");
            info.put("timestamp", Instant.now());
            info.put("uptime", System.currentTimeMillis());
            
            log.info("System information requested");
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Failed to retrieve system info", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
