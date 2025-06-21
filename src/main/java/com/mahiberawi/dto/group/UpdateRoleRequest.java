package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private GroupMemberRole role;
} 