package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterNotificationEmailData {
    private String recruiterEmail;
    private String candidateName;
    private String jobTitle;
    private LocalDateTime applicationDate;
    private String dashboardUrl;
}