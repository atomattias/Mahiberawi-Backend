package com.mahiberawi.dto.group;

import com.mahiberawi.dto.message.MessageResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupPostsResponse {
    private String groupId;
    private String groupName;
    private String userRole;
    private List<MessageResponse> posts;
    private Boolean canCreatePosts;
    private Integer totalPosts;
} 