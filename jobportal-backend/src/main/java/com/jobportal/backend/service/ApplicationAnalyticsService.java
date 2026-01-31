package com.jobportal.backend.service;

import com.jobportal.backend.dto.ApplicationAnalyticsResponse;

public interface ApplicationAnalyticsService {
    ApplicationAnalyticsResponse getAnalytics();
    ApplicationAnalyticsResponse getRecruiterAnalytics(Long recruiterId);
}