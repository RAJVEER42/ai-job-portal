package com.jobportal.backend.dto;
import com.jobportal.backend.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private String phone;
    private LocalDateTime createdAt;
    // DO NOT add any getEmail() or getFullName() methods!
    // Lombok @Data already generates them automatically
}