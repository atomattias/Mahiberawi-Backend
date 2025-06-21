package com.mahiberawi.dto;

import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserIntention;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private UserRole role;
    private UserIntention intention;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int groupCount;
    private int createdGroups;
} 