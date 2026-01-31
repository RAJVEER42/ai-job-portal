package com.jobportal.backend.controller;

import com.jobportal.backend.dto.*;
import com.jobportal.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIResumeParserService resumeParserService;
    private final JobRecommendationService jobRecommendationService;
    private final SkillGapAnalysisService skillGapAnalysisService;
    private final AIInterviewQuestionService interviewQuestionService;

    // Resume Parser
    @PostMapping("/parse-resume")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ParsedResumeResponse>> parseResume(
            @RequestParam("file") MultipartFile file) {
        
        log.info("POST /api/ai/parse-resume - file: {}", file.getOriginalFilename());
        
        ParsedResumeResponse result = resumeParserService.parseResume(file);
        
        return ResponseEntity.ok(ApiResponse.<ParsedResumeResponse>builder()
                .success(true)
                .message("Resume parsed successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Job Recommendations
    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<JobRecommendationResponse>>> getRecommendations(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        log.info("GET /api/ai/recommendations - userId: {}, limit: {}", userId, limit);
        
        List<JobRecommendationResponse> recommendations = 
                jobRecommendationService.getRecommendations(userId, limit);
        
        return ResponseEntity.ok(ApiResponse.<List<JobRecommendationResponse>>builder()
                .success(true)
                .message("Job recommendations generated successfully")
                .data(recommendations)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Skill Gap Analysis
    @GetMapping("/skill-gap-analysis")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ApiResponse<SkillGapAnalysisResponse>> analyzeSkillGap(
            @RequestParam Long userId,
            @RequestParam Long jobId) {
        
        log.info("GET /api/ai/skill-gap-analysis - userId: {}, jobId: {}", userId, jobId);
        
        SkillGapAnalysisResponse analysis = skillGapAnalysisService.analyzeSkillGap(userId, jobId);
        
        return ResponseEntity.ok(ApiResponse.<SkillGapAnalysisResponse>builder()
                .success(true)
                .message("Skill gap analysis completed successfully")
                .data(analysis)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Interview Questions Generator
    @PostMapping("/generate-interview-questions")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InterviewQuestionResponse>>> generateInterviewQuestions(
            @Valid @RequestBody GenerateInterviewQuestionsRequest request) {
        
        log.info("POST /api/ai/generate-interview-questions - jobId: {}, difficulty: {}, count: {}", 
                request.getJobId(), request.getDifficulty(), request.getCount());
        
        List<InterviewQuestionResponse> questions = 
                interviewQuestionService.generateQuestions(request);
        
        return ResponseEntity.ok(ApiResponse.<List<InterviewQuestionResponse>>builder()
                .success(true)
                .message(String.format("Generated %d interview questions successfully", questions.size()))
                .data(questions)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
