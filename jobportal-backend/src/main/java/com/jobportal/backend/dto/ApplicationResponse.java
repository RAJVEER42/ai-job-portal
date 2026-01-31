package com.jobportal.backend.dto;

import com.jobportal.backend.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private ApplicationStatus status;
    private String resumeUrl;
    private String coverLetter;
    private String recruiterNotes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    
    private CandidateInfo candidate;
    private JobInfo job;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfo {
        private Long id;
        private String title;
        private String company;
        private String location;
    }
}