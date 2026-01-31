package com.jobportal.backend.service;

import com.jobportal.backend.dto.ApplicationResponse;
import com.jobportal.backend.dto.CreateApplicationRequest;
import com.jobportal.backend.dto.UpdateApplicationStatusRequest;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse applyToJob(CreateApplicationRequest request, Long userId);
    ApplicationResponse getApplicationById(Long id);
    List<ApplicationResponse> getUserApplications(Long userId);
    List<ApplicationResponse> getJobApplications(Long jobId);
    List<ApplicationResponse> getRecruiterApplications(Long recruiterId);
    ApplicationResponse updateApplicationStatus(Long applicationId, UpdateApplicationStatusRequest request, Long recruiterId);
    void withdrawApplication(Long applicationId, Long userId);
    long getApplicationCountForJob(Long jobId);
    boolean hasUserApplied(Long userId, Long jobId);
}