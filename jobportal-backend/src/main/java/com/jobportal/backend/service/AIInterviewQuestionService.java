package com.jobportal.backend.service;

import com.jobportal.backend.dto.GenerateInterviewQuestionsRequest;
import com.jobportal.backend.dto.InterviewQuestionResponse;
import java.util.List;

public interface AIInterviewQuestionService {
    List<InterviewQuestionResponse> generateQuestions(GenerateInterviewQuestionsRequest request);
}