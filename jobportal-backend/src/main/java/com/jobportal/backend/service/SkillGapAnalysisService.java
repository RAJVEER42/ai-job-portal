package com.jobportal.backend.service;

import com.jobportal.backend.dto.SkillGapAnalysisResponse;

public interface SkillGapAnalysisService {
    SkillGapAnalysisResponse analyzeSkillGap(Long userId, Long jobId);
}