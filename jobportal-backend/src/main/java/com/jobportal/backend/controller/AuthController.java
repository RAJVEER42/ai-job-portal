package com.jobportal.backend.controller;

import com.jobportal.backend.dto.*;
import com.jobportal.backend.service.AuthService;
import com.jobportal.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication including login, registration, and token management")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account. This endpoint is also available at /api/users/register for consistency."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "User registered successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Email already exists"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        try {
            UserResponse userResponse = userService.registerUser(request);
            log.info("User registered successfully: {}", userResponse.getEmail());
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);  // This was missing!
        }
    }
    
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        try {
            AuthResponse authResponse = authService.login(request);
            
            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(authResponse)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            
            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    // POST /api/auth/refresh
    // ...existing code...
@PostMapping("/refresh")
public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    log.info("Refresh token request received");
    
    try {
        AuthResponse authResponse = authService.refreshToken(request);
        
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(authResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
        
    } catch (RuntimeException e) {
        log.error("Token refresh failed: {}", e.getMessage());
        
        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .success(false)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
// ...existing code...
    
}