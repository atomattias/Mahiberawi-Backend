package com.mahiberawi.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private boolean success;
    private String message;
    private boolean requiresVerification;
    private String email;
    private AuthResponse authResponse;
} 