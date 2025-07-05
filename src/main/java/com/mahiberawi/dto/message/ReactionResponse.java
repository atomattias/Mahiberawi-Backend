package com.mahiberawi.dto.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ReactionResponse {
    private String postId;
    private Map<String, Integer> reactions; // reaction type -> count
    private String userReaction; // current user's reaction
    private LocalDateTime updatedAt;
} 