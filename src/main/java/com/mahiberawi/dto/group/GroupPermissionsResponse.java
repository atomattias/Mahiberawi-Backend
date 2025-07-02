package com.mahiberawi.dto.group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupPermissionsResponse {
    private String groupId;
    private String groupName;
    private String userRole;
    
    // Permission flags
    private Boolean canCreateEvents;
    private Boolean canCreatePosts;
    private Boolean canCreatePayments;
    private Boolean canSendInvitations;
    private Boolean canRevokeInvitations;
    private Boolean canManageMembers;
    private Boolean canUpdateGroupSettings;
    private Boolean canDeleteGroup;
    private Boolean canViewMembers;
    private Boolean canViewEvents;
    private Boolean canViewPosts;
    private Boolean canViewPayments;
    private Boolean canViewInvitations;
    
    // Group settings that affect permissions
    private Boolean allowEventCreation;
    private Boolean allowMemberInvites;
    private Boolean allowMessagePosting;
    private Boolean paymentRequired;
    private Boolean requireApproval;
} 