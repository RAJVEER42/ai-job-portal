package com.jobportal.backend.service;

import com.jobportal.backend.dto.ParsedResumeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AIResumeParserService {
    ParsedResumeResponse parseResume(MultipartFile file);
    String extractTextFromFile(MultipartFile file);
}