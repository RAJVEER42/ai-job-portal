package com.jobportal.backend.dto;

import com.jobportal.backend.dto.InterviewQuestionResponse.QuestionDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInterviewQuestionsRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    private QuestionDifficulty difficulty;
    
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count cannot exceed 20")
    private Integer count = 10;
}