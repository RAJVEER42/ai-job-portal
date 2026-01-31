package com.jobportal.backend.controller;

import com.jobportal.backend.dto.ApiResponse;
import com.jobportal.backend.dto.RegisterRequest;
import com.jobportal.backend.dto.UserResponse;
import com.jobportal.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration, profile management, and user operations")
public class UserController {
    
    private final UserService userService;
    
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided details. The user can be a CANDIDATE, RECRUITER, or ADMIN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Email already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid input data"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        
        UserResponse userResponse = userService.registerUser(request);
        
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves user information by their unique identifier"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User found successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        
        UserResponse userResponse = userService.getUserById(id);
        
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Get user by email",
        description = "Retrieves user information by their email address"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User found successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "User not found"
        )
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
        @Parameter(description = "User email address", required = true, example = "user@example.com")
        @PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        
        UserResponse userResponse = userService.getUserByEmail(email);
        
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(userResponse)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
