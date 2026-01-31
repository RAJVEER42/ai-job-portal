package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetEmailData {
    private String email;
    private String resetToken;
    private LocalDateTime expirationTime;
    private String resetUrl;
}