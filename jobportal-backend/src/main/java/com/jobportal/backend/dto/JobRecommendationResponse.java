package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationResponse {
    
    private JobSummary job;
    private Integer matchScore;
    private List<String> matchReasons;
    private List<String> missingSkills;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobSummary {
        private Long id;
        private String title;
        private String company;
        private String location;
        private String jobType;
        private String experienceLevel;
        private List<String> requiredSkills;
    }
}