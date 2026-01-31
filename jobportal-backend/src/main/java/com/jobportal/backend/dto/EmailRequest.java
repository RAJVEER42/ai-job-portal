package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String template;
    private Object templateData;
    private String priority; // HIGH, MEDIUM, LOW
}
