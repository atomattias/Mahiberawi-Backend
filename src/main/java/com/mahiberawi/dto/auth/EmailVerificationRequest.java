package com.mahiberawi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Verification code is required")
    private String code;
} 