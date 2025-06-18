package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupMemberResponse {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String userProfilePicture;
    private String groupId;
    private String groupName;
    private GroupMemberRole role;
    private GroupMemberStatus status;
    private String invitedById;
    private String invitedByName;
    private String invitationMessage;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 