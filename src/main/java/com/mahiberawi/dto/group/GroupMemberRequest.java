package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupMemberRequest {
    @Email(message = "Invalid email format")
    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private GroupMemberRole role;
} 