package com.jobportal.backend.service;

import com.jobportal.backend.dto.RegisterRequest;
import com.jobportal.backend.dto.UserResponse;

public interface UserService {
    UserResponse registerUser(RegisterRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    void updateResumeUrl(Long userId, String resumeUrl);
}