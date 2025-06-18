package com.mahiberawi.dto.membership;

import com.mahiberawi.entity.MembershipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MembershipRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Membership type is required")
    private MembershipType type;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Positive(message = "Fee must be positive")
    private Double fee;

    private String description;
} 