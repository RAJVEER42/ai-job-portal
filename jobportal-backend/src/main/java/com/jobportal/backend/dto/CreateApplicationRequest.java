package com.jobportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @NotBlank(message = "Cover letter is required")
    private String coverLetter;
    
    private String resumeUrl; // Optional - can use user's default resume
}