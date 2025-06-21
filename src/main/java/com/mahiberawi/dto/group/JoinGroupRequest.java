package com.mahiberawi.dto.group;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinGroupRequest {
    @NotBlank(message = "Group code is required")
    private String code; // Group invitation code
} 