package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import com.jobportal.backend.dto.FileUploadResponse;
import com.jobportal.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(originPatterns = "*", allowCredentials = "false")  // ADDED THIS LINE
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        log.info("File upload request received: {}", file.getOriginalFilename());
        
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }
            
            String contentType = file.getContentType();
            if (!isValidFileType(contentType)) {
                throw new RuntimeException("Invalid file type. Only PDF, DOCX, and DOC are allowed");
            }
            
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds 10MB limit");
            }
            
            String fileUrl = fileStorageService.storeFile(file);
            
            FileUploadResponse response = FileUploadResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileType(contentType)
                    .fileSize(file.getSize())
                    .build();
            
            log.info("File uploaded successfully: {}", fileUrl);
            
            return ResponseEntity.ok(ApiResponse.<FileUploadResponse>builder()
                    .success(true)
                    .message("File uploaded successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
            
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<FileUploadResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
    
    private boolean isValidFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/msword")
        );
    }
}