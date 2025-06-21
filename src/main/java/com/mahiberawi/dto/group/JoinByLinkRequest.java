package com.mahiberawi.dto.group;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinByLinkRequest {
    @NotBlank(message = "Invitation link is required")
    private String invitationLink;
} 