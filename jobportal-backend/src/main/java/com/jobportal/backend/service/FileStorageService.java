package com.jobportal.backend.service;
import org.springframework.web.multipart.MultipartFile;
public interface FileStorageService {
String storeFile(MultipartFile file);
}