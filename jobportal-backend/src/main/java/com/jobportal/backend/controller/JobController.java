package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import com.jobportal.backend.dto.ApplicationResponse;
import com.jobportal.backend.dto.CreateApplicationRequest;
import com.jobportal.backend.dto.CreateJobRequest;
import com.jobportal.backend.dto.JobResponse;
import com.jobportal.backend.service.ApplicationService;
import com.jobportal.backend.service.JobService;
import com.jobportal.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class JobController {
    
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;
    
    // Only RECRUITERs can create jobs
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        log.info("Creating job: {} by recruiter: {}", request.getTitle(), email);
        
        JobResponse jobResponse = jobService.createJob(request, 2L);  // TODO: Get from token
        
        ApiResponse<JobResponse> response = ApiResponse.<JobResponse>builder()
                .success(true)
                .message("Job created successfully")
                .data(jobResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // Public - anyone can view jobs
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        log.info("Fetching job with ID: {}", id);
        
        JobResponse jobResponse = jobService.getJobById(id);
        
        ApiResponse<JobResponse> response = ApiResponse.<JobResponse>builder()
                .success(true)
                .message("Job fetched successfully")
                .data(jobResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Only authenticated CANDIDATEs can apply to jobs
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @PathVariable Long id,
            @RequestBody CreateApplicationRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} applying for job {}", userEmail, id);
        
        try {
            // Get user ID from email
            Long userId = userService.getUserByEmail(userEmail).getId();
            
            // Set the job ID from the path parameter (override any existing value)
            request.setJobId(id);
            
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
    
    // Public - anyone can view jobs
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getAllActiveJobs() {
        log.info("Fetching all active jobs");
        
        List<JobResponse> jobs = jobService.getAllActiveJobs();
        
        ApiResponse<List<JobResponse>> response = ApiResponse.<List<JobResponse>>builder()
                .success(true)
                .message("Jobs fetched successfully")
                .data(jobs)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Public
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllActiveJobsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("Fetching jobs with pagination: page={}, size={}, sortBy={}, direction={}", 
                 page, size, sortBy, direction);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<JobResponse> jobs = jobService.getAllActiveJobsPaginated(pageable);
        
        ApiResponse<Page<JobResponse>> response = ApiResponse.<Page<JobResponse>>builder()
                .success(true)
                .message("Jobs fetched successfully")
                .data(jobs)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Public
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<JobResponse>>> searchJobs(@RequestParam String keyword) {
        log.info("Searching jobs with keyword: {}", keyword);
        
        List<JobResponse> jobs = jobService.searchJobs(keyword);
        
        ApiResponse<List<JobResponse>> response = ApiResponse.<List<JobResponse>>builder()
                .success(true)
                .message("Search results fetched successfully")
                .data(jobs)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Public
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobsByLocation(@PathVariable String location) {
        log.info("Fetching jobs in location: {}", location);
        
        List<JobResponse> jobs = jobService.getJobsByLocation(location);
        
        ApiResponse<List<JobResponse>> response = ApiResponse.<List<JobResponse>>builder()
                .success(true)
                .message("Jobs fetched successfully")
                .data(jobs)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Public
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getJobsByRecruiter(@PathVariable Long recruiterId) {
        log.info("Fetching jobs posted by recruiter ID: {}", recruiterId);
        
        List<JobResponse> jobs = jobService.getJobsByRecruiterId(recruiterId);
        
        ApiResponse<List<JobResponse>> response = ApiResponse.<List<JobResponse>>builder()
                .success(true)
                .message("Jobs fetched successfully")
                .data(jobs)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Only RECRUITER who owns the job can update
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody CreateJobRequest request,
            @RequestParam Long recruiterId) {
        
        log.info("Updating job ID: {} by recruiter ID: {}", id, recruiterId);
        
        JobResponse jobResponse = jobService.updateJob(id, request, recruiterId);
        
        ApiResponse<JobResponse> response = ApiResponse.<JobResponse>builder()
                .success(true)
                .message("Job updated successfully")
                .data(jobResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    // Only RECRUITER who owns the job can delete
    @PreAuthorize("hasRole('RECRUITER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @RequestParam Long recruiterId) {
        
        log.info("Deleting job ID: {} by recruiter ID: {}", id, recruiterId);
        
        jobService.deleteJob(id, recruiterId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Job deleted successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}