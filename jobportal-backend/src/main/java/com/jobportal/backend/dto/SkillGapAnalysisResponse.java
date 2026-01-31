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
public class SkillGapAnalysisResponse {
    
    private Integer matchPercentage;
    private List<SkillMatch> matchingSkills;
    private List<MissingSkill> missingSkills;
    private List<String> recommendations;
    private JobInfo jobInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatch {
        private String skill;
        private String userLevel;
        private String requiredLevel;
        private MatchStatus status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingSkill {
        private String skill;
        private String requiredLevel;
        private SkillPriority priority;
        private List<LearningResource> learningResources;
        private String estimatedLearningTime;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningResource {
        private String title;
        private String url;
        private String duration;
        private ResourceType type;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfo {
        private Long jobId;
        private String title;
        private String company;
        private String experienceLevel;
    }
    
    public enum MatchStatus {
        EXCEEDS,    // User skill level exceeds requirement
        MATCHES,    // Perfect match
        BELOW       // User skill below requirement
    }
    
    public enum SkillPriority {
        HIGH,       // Critical skill for the role
        MEDIUM,     // Important but not critical
        LOW         // Nice to have
    }
    
    public enum ResourceType {
        VIDEO,
        COURSE,
        DOCUMENTATION,
        TUTORIAL,
        BOOK
    }
}