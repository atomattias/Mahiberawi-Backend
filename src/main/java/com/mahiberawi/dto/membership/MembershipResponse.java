package com.mahiberawi.dto.membership;

import com.mahiberawi.entity.MembershipStatus;
import com.mahiberawi.entity.MembershipType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MembershipResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userFullName;
    private String userProfilePicture;
    private String groupId;
    private String groupName;
    private MembershipType type;
    private MembershipStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal fee;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastPaymentDate;
    private LocalDateTime nextPaymentDate;
} 