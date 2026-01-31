package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import com.jobportal.backend.dto.ApplicationResponse;
import com.jobportal.backend.dto.CreateApplicationRequest;
import com.jobportal.backend.dto.UpdateApplicationStatusRequest;
import com.jobportal.backend.service.ApplicationService;
import com.jobportal.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @Valid @RequestBody CreateApplicationRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} applying for job {}", userEmail, request.getJobId());
        
        try {
            // Get user ID from email
            Long userId = userService.getUserByEmail(userEmail).getId();
            
            ApplicationResponse applicationResponse = applicationService.applyToJob(request, userId);
            
            ApiResponse<ApplicationResponse> response = ApiResponse.<ApplicationResponse>builder()
                    .success(true)
                    .message("Application submitted successfully")
                    .data(applicationResponse)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Application failed: {}", e.getMessage());
            
            ApiResponse<ApplicationResponse> response = ApiResponse.<ApplicationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("Fetching applications for user: {}", userEmail);
        
        try {
            Long userId = userService.getUserByEmail(userEmail).getId();
            List<ApplicationResponse> applications = applicationService.getUserApplications(userId);
            
            ApiResponse<List<ApplicationResponse>> response = ApiResponse.<List<ApplicationResponse>>builder()
                    .success(true)
                    .message("Applications fetched successfully")
                    .data(applications)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to fetch applications: {}", e.getMessage());
            
            ApiResponse<List<ApplicationResponse>> response = ApiResponse.<List<ApplicationResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Compatibility endpoint - redirect /my to /my-applications
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplicationsCompatibility(
            Authentication authentication) {
        log.info("Redirecting /my to /my-applications for user: {}", authentication.getName());
        return getMyApplications(authentication);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getJobApplications(
            @PathVariable Long jobId,
            Authentication authentication) {
        
        String recruiterEmail = authentication.getName();
        log.info("Fetching applications for job {} by recruiter {}", jobId, recruiterEmail);
        
        try {
            List<ApplicationResponse> applications = applicationService.getJobApplications(jobId);
            
            ApiResponse<List<ApplicationResponse>> response = ApiResponse.<List<ApplicationResponse>>builder()
                    .success(true)
                    .message("Job applications fetched successfully")
                    .data(applications)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to fetch job applications: {}", e.getMessage());
            
            ApiResponse<List<ApplicationResponse>> response = ApiResponse.<List<ApplicationResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication) {
        
        String recruiterEmail = authentication.getName();
        log.info("Updating application {} status by recruiter {}", applicationId, recruiterEmail);
        
        try {
            Long recruiterId = userService.getUserByEmail(recruiterEmail).getId();
            ApplicationResponse applicationResponse = applicationService.updateApplicationStatus(
                    applicationId, request, recruiterId);
            
            ApiResponse<ApplicationResponse> response = ApiResponse.<ApplicationResponse>builder()
                    .success(true)
                    .message("Application status updated successfully")
                    .data(applicationResponse)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to update application status: {}", e.getMessage());
            
            ApiResponse<ApplicationResponse> response = ApiResponse.<ApplicationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<String>> withdrawApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} withdrawing application {}", userEmail, applicationId);
        
        try {
            Long userId = userService.getUserByEmail(userEmail).getId();
            applicationService.withdrawApplication(applicationId, userId);
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Application withdrawn successfully")
                    .data("Application ID: " + applicationId)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to withdraw application: {}", e.getMessage());
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}