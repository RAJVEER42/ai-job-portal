package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedResumeResponse {
    
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private List<Experience> experience;
    private List<Education> education;
    private String summary;
    private String totalExperience;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Experience {
        private String company;
        private String role;
        private String duration;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String degree;
        private String institution;
        private String year;
        private String fieldOfStudy;
    }
}