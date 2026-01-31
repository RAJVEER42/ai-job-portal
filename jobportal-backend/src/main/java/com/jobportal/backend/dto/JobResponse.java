package com.jobportal.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jobportal.backend.model.JobType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String experienceRequired;
    private String skillsRequired;
    private JobType jobType;
    private LocalDate applicationDeadline;
    private Boolean isActive;
    private LocalDateTime postedAt;
    private RecruiterInfo recruiter;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruiterInfo {
        private Long id;
        private String fullName;
        private String email;
    }
}