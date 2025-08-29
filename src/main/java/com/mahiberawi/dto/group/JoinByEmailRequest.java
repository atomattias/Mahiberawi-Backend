package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinByEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String groupCode; // Optional group code for direct joining
    
    private GroupMemberRole role = GroupMemberRole.MEMBER; // Default role, can be overridden
} 