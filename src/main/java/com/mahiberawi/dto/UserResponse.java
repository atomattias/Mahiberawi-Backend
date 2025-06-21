package com.mahiberawi.dto;

import com.mahiberawi.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private LocalDateTime createdAt;
} 