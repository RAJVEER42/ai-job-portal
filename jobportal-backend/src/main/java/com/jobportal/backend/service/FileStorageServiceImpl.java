package com.jobportal.backend.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
private final Path fileStorageLocation;

public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    
    try {
        Files.createDirectories(this.fileStorageLocation);
        log.info("File storage location created: {}", this.fileStorageLocation);
    } catch (IOException e) {
        throw new RuntimeException("Could not create upload directory", e);
    }
}

@Override
public String storeFile(MultipartFile file) {
    try {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File stored successfully: {}", fileName);
        
        return "/api/files/" + fileName;
        
    } catch (IOException e) {
        log.error("Failed to store file: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to store file", e);
    }
}
}