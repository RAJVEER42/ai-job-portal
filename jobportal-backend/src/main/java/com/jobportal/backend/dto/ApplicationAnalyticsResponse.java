package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationAnalyticsResponse {
    
    private OverallStats overallStats;
    private List<JobPopularity> popularJobs;
    private List<SkillDemand> skillDemands;
    private ApplicationTrends applicationTrends;
    private List<RecruitmentMetric> recruitmentMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Long totalApplications;
        private Long totalJobs;
        private Long totalCandidates;
        private Long activeJobs;
        private Double averageApplicationsPerJob;
        private Map<String, Long> applicationsByStatus;
        private Double successRate; // Percentage of hired applications
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPopularity {
        private Long jobId;
        private String title;
        private String company;
        private String location;
        private Long applicationCount;
        private Long viewCount; // If you track views
        private Double conversionRate;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDemand {
        private String skill;
        private Long demandCount; // Number of jobs requiring this skill
        private Long candidateCount; // Number of candidates with this skill
        private Double supplyDemandRatio;
        private TrendDirection trend;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationTrends {
        private Map<String, Long> applicationsByMonth;
        private Map<String, Long> applicationsByWeek;
        private Map<String, Double> successRateByMonth;
        private String peakApplicationDay;
        private String mostActiveTimeSlot;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentMetric {
        private Long jobId;
        private String jobTitle;
        private Integer daysToFirstApplication;
        private Integer daysToHire;
        private Integer totalApplicants;
        private Integer shortlistedCount;
        private Integer interviewedCount;
        private Integer hiredCount;
        private Double qualityScore; // Based on shortlist/application ratio
    }
    
    public enum TrendDirection {
        UP,
        DOWN,
        STABLE
    }
}