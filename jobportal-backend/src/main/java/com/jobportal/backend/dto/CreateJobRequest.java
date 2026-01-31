package com.jobportal.backend.dto;
import com.jobportal.backend.model.JobType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Location is required")
    private String location;

    @Min(value = 0, message = "Minimum salary must be positive")
    private BigDecimal minSalary;

    @Min(value = 0, message = "Maximum salary must be positive")
    private BigDecimal maxSalary;

    @NotBlank(message = "Experience required is required")
    private String experienceRequired;

    @NotBlank(message = "Skills required is required")
    private String skillsRequired;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @Future(message = "Application deadline must be in the future")
    private LocalDate applicationDeadline;
}