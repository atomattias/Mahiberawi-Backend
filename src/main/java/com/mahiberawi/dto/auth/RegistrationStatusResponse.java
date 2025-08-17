package com.mahiberawi.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationStatusResponse {
    private String status; // "PENDING_VERIFICATION", "VERIFIED", "EXPIRED", "NOT_FOUND"
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long timeRemaining; // seconds until expiration
    private Boolean canResend;
    private String message;
    private Boolean success;
}
