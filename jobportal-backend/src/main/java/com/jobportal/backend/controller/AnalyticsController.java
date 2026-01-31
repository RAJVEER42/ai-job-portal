package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import com.jobportal.backend.dto.ApplicationAnalyticsResponse;
import com.jobportal.backend.service.ApplicationAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final ApplicationAnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'CANDIDATE')") // ✅ Added CANDIDATE
    public ResponseEntity<ApiResponse<ApplicationAnalyticsResponse>> getOverallAnalytics() {
        log.info("GET /api/analytics/overview");
        
        ApplicationAnalyticsResponse analytics = analyticsService.getAnalytics();
        
        return ResponseEntity.ok(ApiResponse.<ApplicationAnalyticsResponse>builder()
                .success(true)
                .message("Analytics retrieved successfully")
                .data(analytics)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/recruiter/{recruiterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'CANDIDATE')") // ✅ Added CANDIDATE
    public ResponseEntity<ApiResponse<ApplicationAnalyticsResponse>> getRecruiterAnalytics(
            @PathVariable Long recruiterId) {
        
        log.info("GET /api/analytics/recruiter/{}", recruiterId);
        
        ApplicationAnalyticsResponse analytics = analyticsService.getRecruiterAnalytics(recruiterId);
        
        return ResponseEntity.ok(ApiResponse.<ApplicationAnalyticsResponse>builder()
                .success(true)
                .message("Recruiter analytics retrieved successfully")
                .data(analytics)
                .timestamp(LocalDateTime.now())
                .build());
    }
}