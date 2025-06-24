package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.InvitationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupInvitationResponse {
    private String id;
    private String groupId;
    private String groupName;
    private String email;
    private String phone;
    private String invitedBy;
    private String inviterName;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String message;
    private String invitationCode; // Only included for code-based invitations
} 