package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinGroupRequest {
    @NotBlank(message = "Group code is required")
    private String code; // Group invitation code
    
    private GroupMemberRole role = GroupMemberRole.MEMBER; // Default role, can be overridden
} 