package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusEmailData {
    private String candidateName;
    private String jobTitle;
    private String companyName;
    private String status; // RECEIVED, UNDER_REVIEW, INTERVIEW_SCHEDULED, ACCEPTED, REJECTED
    private String statusMessage;
    private String nextSteps;
    private String contactEmail;
}
