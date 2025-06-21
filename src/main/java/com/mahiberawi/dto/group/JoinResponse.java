package com.mahiberawi.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinResponse {
    private boolean success;
    private String message;
    private boolean requiresVerification;
    private String email;
    private GroupResponse group;
    private String invitationToken; // For email verification flow
} 