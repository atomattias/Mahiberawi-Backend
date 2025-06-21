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
    private String name;
    private String email;
    private GroupMemberRole role;
    private GroupMemberStatus status;
    private LocalDateTime joinedAt;
} 