package com.jobportal.backend.service;

import com.jobportal.backend.dto.AuthResponse;
import com.jobportal.backend.dto.LoginRequest;
import com.jobportal.backend.dto.RefreshTokenRequest;
import com.jobportal.backend.dto.UserResponse;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.UserRepository;
import com.jobportal.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // ...existing code...
@Override
public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
    String accessToken = jwtUtil.generateToken(userDetails);
    String refreshToken = jwtUtil.generateRefreshToken(userDetails);  // Fix: use generateRefreshToken

    return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getExpirationTime())
            .user(mapToUserResponse(user))
            .build();
}
// ...existing code...
    @Override
public AuthResponse refreshToken(RefreshTokenRequest request) {
    String username = jwtUtil.extractUsername(request.getRefreshToken());
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    if (jwtUtil.validateToken(request.getRefreshToken(), userDetails)) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(userDetails);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())  // Keep same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }
    throw new RuntimeException("Invalid refresh token");
}
// ...existing code...

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }
}