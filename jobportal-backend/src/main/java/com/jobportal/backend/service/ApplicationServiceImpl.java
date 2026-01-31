package com.jobportal.backend.service;

import com.jobportal.backend.dto.ApplicationResponse;
import com.jobportal.backend.dto.ApplicationStatusEmailData;
import com.jobportal.backend.dto.CreateApplicationRequest;
import com.jobportal.backend.dto.UpdateApplicationStatusRequest;
import com.jobportal.backend.exception.DuplicateResourceException;
import com.jobportal.backend.exception.ResourceNotFoundException;
import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.ApplicationStatus;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.ApplicationRepository;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;
    
    @Override
    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public ApplicationResponse applyToJob(CreateApplicationRequest request, Long userId) {
        log.info("User {} applying to job {}", userId, request.getJobId());
        
        // 1. Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // 2. Check if job exists
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));
        
        // 3. Check if job is active
        if (!job.getIsActive()) {
            throw new RuntimeException("Cannot apply to inactive job");
        }
        
        // 4. Check if user already applied
        if (applicationRepository.existsByUserIdAndJobId(userId, request.getJobId())) {
            throw new DuplicateResourceException("You have already applied to this job");
        }
        
        // 5. Check if application deadline passed
        if (job.getApplicationDeadline() != null && 
            ((LocalDate) job.getApplicationDeadline()).isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Application deadline has passed");
        }
        
        // 6. Create application
        Application application = Application.builder()
                .user(user)
                .job(job)
                .resumeUrl(request.getResumeUrl())
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.PENDING)
                .build();
        
        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with ID: {}", savedApplication.getId());
        
        // 7. Send email notifications asynchronously
        sendApplicationNotifications(user, job, savedApplication);
        
        return mapToApplicationResponse(savedApplication);
    }
    
    @Override
    @Cacheable(value = "applications", key = "#id")
    public ApplicationResponse getApplicationById(Long id) {
        log.info("Fetching application with ID: {}", id);
        
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));
        
        return mapToApplicationResponse(application);
    }
    
    @Override
    @Cacheable(value = "applications", key = "'user-' + #userId")
    public List<ApplicationResponse> getUserApplications(Long userId) {
        log.info("Fetching applications for user ID: {}", userId);
        
        return applicationRepository.findByUserId(userId)
                .stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApplicationResponse> getJobApplications(Long jobId) {
        log.info("Fetching applications for job ID: {}", jobId);
        
        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ApplicationResponse> getRecruiterApplications(Long recruiterId) {
        log.info("Fetching applications for recruiter ID: {}", recruiterId);
        
        return applicationRepository.findApplicationsForRecruiter(recruiterId)
                .stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "applications", allEntries = true)
    public ApplicationResponse updateApplicationStatus(Long applicationId, 
                                                    UpdateApplicationStatusRequest request, 
                                                    Long recruiterId) {
        log.info("Updating application {} status to {} by recruiter {}", 
                applicationId, request.getStatus(), recruiterId);
    
        // Use custom query that fetches everything
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
    
        // Now we can safely access recruiter without LazyInitializationException
        Long jobRecruiterId = application.getJob().getRecruiter().getId();
    
        // Verify recruiter owns the job
        if (!jobRecruiterId.equals(recruiterId)) {
            log.error("Unauthorized: Recruiter {} tried to update application for job owned by recruiter {}", 
                    recruiterId, jobRecruiterId);
            throw new RuntimeException("You are not authorized to update this application");
        }

        // Store old status for comparison
        ApplicationStatus oldStatus = application.getStatus();
    
        // Update status and notes
        application.setStatus(request.getStatus());
        if (request.getRecruiterNotes() != null) {
            application.setRecruiterNotes(request.getRecruiterNotes());
        }
    
        Application updatedApplication = applicationRepository.save(application);
        log.info("Application status updated successfully to {}", updatedApplication.getStatus());

        // Send status update email if status changed
        if (!oldStatus.equals(request.getStatus())) {
            sendStatusUpdateEmail(updatedApplication);
        }
    
        return mapToApplicationResponse(updatedApplication);
    }
    
    @Override
    @Transactional
    public void withdrawApplication(Long applicationId, Long userId) {
        log.info("User {} withdrawing application {}", userId, applicationId);
        
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        
        // Verify user owns the application
        if (!application.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to withdraw this application");
        }
        
        // Check if already processed
        if (application.getStatus() == ApplicationStatus.ACCEPTED || 
            application.getStatus() == ApplicationStatus.REJECTED) {
            throw new RuntimeException("Cannot withdraw application that is already " + application.getStatus());
        }
        
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
        log.info("Application withdrawn successfully");
    }
    
    @Override
    public long getApplicationCountForJob(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }
    
    @Override
    public boolean hasUserApplied(Long userId, Long jobId) {
        return applicationRepository.existsByUserIdAndJobId(userId, jobId);
    }
    
    // Helper: Entity → DTO conversion
    // ...existing code...
private ApplicationResponse mapToApplicationResponse(Application application) {
    return ApplicationResponse.builder()
            .id(application.getId())
            .status(application.getStatus())
            .resumeUrl(application.getResumeUrl())
            .coverLetter(application.getCoverLetter())
            .recruiterNotes(application.getRecruiterNotes())
            .appliedAt(application.getAppliedAt())
            .updatedAt(application.getUpdatedAt())
            .candidate(ApplicationResponse.CandidateInfo.builder()
                    .id(application.getUser().getId())
                    .fullName(application.getUser().getFullName())
                    .email(application.getUser().getEmail())
                    .phone(application.getUser().getPhone())
                    .build())
            .job(ApplicationResponse.JobInfo.builder()
                    .id(application.getJob().getId())
                    .title(application.getJob().getTitle())  // Fixed: was casting to String
                    .company(application.getJob().getCompany())  // Fixed: was casting to String
                    .location(application.getJob().getLocation())  // Fixed: was using description
                    .build())
            .build();
    }

    /**
     * Send email notifications when a new application is submitted
     */
    private void sendApplicationNotifications(User candidate, Job job, Application application) {
        try {
            // 1. Send confirmation email to candidate
            ApplicationStatusEmailData candidateData = ApplicationStatusEmailData.builder()
                    .candidateName(candidate.getFullName())
                    .jobTitle(job.getTitle())
                    .companyName(job.getCompany())
                    .status("RECEIVED")
                    .statusMessage("Thank you for applying! We have received your application and will review it shortly.")
                    .nextSteps("Our recruitment team will review your application and contact you within 3-5 business days if your profile matches our requirements.")
                    .contactEmail(job.getRecruiter().getEmail())
                    .build();

            emailService.sendApplicationStatusEmail(candidate.getEmail(), candidateData);

            // 2. Send notification email to recruiter
            emailService.sendRecruiterNotificationEmail(
                    job.getRecruiter().getEmail(),
                    candidate.getFullName(),
                    job.getTitle()
            );

            log.info("Application notification emails sent for application ID: {}", application.getId());

        } catch (Exception e) {
            log.error("Failed to send application notification emails for application ID: {}. Error: {}", 
                    application.getId(), e.getMessage());
        }
    }

    /**
     * Send email notification when application status is updated
     */
    private void sendStatusUpdateEmail(Application application) {
        try {
            String statusMessage = getStatusMessage(application.getStatus());
            String nextSteps = getNextSteps(application.getStatus());

            ApplicationStatusEmailData statusData = ApplicationStatusEmailData.builder()
                    .candidateName(application.getUser().getFullName())
                    .jobTitle(application.getJob().getTitle())
                    .companyName(application.getJob().getCompany())
                    .status(application.getStatus().toString())
                    .statusMessage(statusMessage)
                    .nextSteps(nextSteps)
                    .contactEmail(application.getJob().getRecruiter().getEmail())
                    .build();

            emailService.sendApplicationStatusEmail(application.getUser().getEmail(), statusData);

        } catch (Exception e) {
            log.error("Failed to send status update email for application ID: {}. Error: {}", 
                    application.getId(), e.getMessage());
        }
    }

    private String getStatusMessage(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "Your application is being reviewed by our recruitment team.";
            case UNDER_REVIEW -> "Great news! Your application has moved to detailed review stage.";
            case SHORTLISTED -> "Excellent! You have been shortlisted for the position.";
            case INTERVIEWED -> "Thank you for the interview. We are reviewing all candidates.";
            case ACCEPTED -> "Fantastic news! We are pleased to offer you the position.";
            case REJECTED -> "Thank you for your interest. While we were impressed with your background, we have decided to proceed with other candidates.";
            case WITHDRAWN -> "Your application has been withdrawn as requested.";
        };
    }

    private String getNextSteps(ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "We will review your application and update you within 3-5 business days.";
            case UNDER_REVIEW -> "Our team is conducting a detailed review. We will contact you soon with next steps.";
            case SHORTLISTED -> "Congratulations! Please check your email for interview details and confirm your availability.";
            case INTERVIEWED -> "We will make a decision within the next few days and contact you with an update.";
            case ACCEPTED -> "Welcome to the team! Our HR department will contact you with onboarding details.";
            case REJECTED -> "We encourage you to apply for other positions that match your skills and experience.";
            case WITHDRAWN -> "If you change your mind, feel free to apply for other open positions.";
        };
    }
}