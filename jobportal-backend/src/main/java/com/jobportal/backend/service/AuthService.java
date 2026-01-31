package com.jobportal.backend.service;

import com.jobportal.backend.dto.AuthResponse;
import com.jobportal.backend.dto.LoginRequest;
import com.jobportal.backend.dto.RefreshTokenRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
}