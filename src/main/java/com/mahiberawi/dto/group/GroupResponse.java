package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupType;
import com.mahiberawi.entity.enums.GroupPrivacy;
import com.mahiberawi.entity.enums.GroupMemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private GroupType type;
    private GroupPrivacy privacy;
    private String code;
    private String inviteLink;
    private int memberCount;
    private GroupMemberRole userRole; // User's role in this group
    private LocalDateTime createdAt;
    
    // Group settings
    private Boolean allowEventCreation;
    private Boolean allowMemberInvites;
    private Boolean allowMessagePosting;
    private Boolean paymentRequired;
    private Boolean requireApproval;
    private Double monthlyDues;
} 