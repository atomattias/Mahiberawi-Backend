package com.mahiberawi.dto.group;

import com.mahiberawi.entity.GroupStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private String creatorId;
    private String creatorName;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GroupMemberResponse> members;
    private int memberCount;
    private int eventCount;
} 