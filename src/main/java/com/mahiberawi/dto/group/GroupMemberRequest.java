package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupMemberRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Role is required")
    private GroupMemberRole role;

    private String invitationMessage;
} 