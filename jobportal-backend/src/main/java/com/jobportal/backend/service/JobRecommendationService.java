package com.jobportal.backend.service;

import com.jobportal.backend.dto.JobRecommendationResponse;
import java.util.List;

public interface JobRecommendationService {
    List<JobRecommendationResponse> getRecommendations(Long userId, Integer limit);
}