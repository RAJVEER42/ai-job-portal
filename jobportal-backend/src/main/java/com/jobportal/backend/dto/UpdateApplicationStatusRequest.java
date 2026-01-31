package com.jobportal.backend.dto;

import com.jobportal.backend.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
    
    private String recruiterNotes;
}