package com.jobportal.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelcomeEmailData {
    private String fullName;
    private String email;
    private String role;
    private String loginUrl;
}
