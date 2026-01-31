package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobNotificationEmailData {
    private String candidateName;
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String jobType;
    private String salary;
    private String applicationDeadline;
    private String jobUrl;
    private String unsubscribeUrl;
}
