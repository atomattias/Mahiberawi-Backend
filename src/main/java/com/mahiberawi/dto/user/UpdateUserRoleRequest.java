package com.mahiberawi.dto.user;

import com.mahiberawi.entity.UserRole;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private UserRole role;
} 