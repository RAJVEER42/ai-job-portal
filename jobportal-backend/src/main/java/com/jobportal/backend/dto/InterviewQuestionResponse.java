package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionResponse {
    
    private String question;
    private QuestionDifficulty difficulty;
    private String category;
    private String expectedAnswer;
    private List<String> tags;
    private List<String> followUpQuestions;
    
    public enum QuestionDifficulty {
        EASY,
        MEDIUM,
        HARD
    }
}