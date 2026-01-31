package com.jobportal.backend.service;

import com.jobportal.backend.dto.RegisterRequest;
import com.jobportal.backend.dto.UserResponse;
import com.jobportal.backend.dto.WelcomeEmailData;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ...existing code...    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse registerUser(RegisterRequest request) {
        log.info("=== Starting user registration ===");
        log.info("Email: {}", request.getEmail());
        log.info("FullName: {}", request.getFullName());
        log.info("Role: {}", request.getRole());
        
        try {
            // Check if email already exists
            boolean exists = userRepository.existsByEmail(request.getEmail());
            log.info("Email exists check: {}", exists);
            
            if (exists) {
                throw new RuntimeException("Email already registered");
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .role(request.getRole())
                    .phone(request.getPhone())
                    .build();
            
            log.info("User object created, saving to database...");
            User savedUser = userRepository.save(user);
            log.info("User saved with ID: {}", savedUser.getId());
            
            // Send welcome email asynchronously
            sendWelcomeEmail(savedUser);
            
            return mapToUserResponse(savedUser);
        } catch (Exception e) {
            log.error("Error in registerUser: ", e);
            throw e;
        }
    }
// ...existing code...

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Override
    public void updateResumeUrl(Long userId, String resumeUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setResumeUrl(resumeUrl);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }

    /**
     * Send welcome email to newly registered user
     */
    private void sendWelcomeEmail(User user) {
        try {
            log.info("Sending welcome email to: {}", user.getEmail());
            
            WelcomeEmailData welcomeData = WelcomeEmailData.builder()
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole().toString())
                    .loginUrl("http://localhost:3000/login")
                    .build();
            
            emailService.sendWelcomeEmail(user.getEmail(), welcomeData);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}. Error: {}", user.getEmail(), e.getMessage());
            // Don't throw exception here as user registration should still succeed
        }
    }
}