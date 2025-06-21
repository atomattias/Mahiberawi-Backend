package com.mahiberawi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneVerificationRequest {
    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Verification code is required")
    private String code;
} 