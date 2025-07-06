package com.mahiberawi.dto.equb;

import com.mahiberawi.entity.enums.EqubRoundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EqubRoundResponse {
    private String id;
    private String groupId;
    private String groupName;
    private Integer roundNumber;
    private BigDecimal totalAmount;
    private BigDecimal expectedAmount;
    private String winnerId;
    private String winnerName;
    private EqubRoundStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime winnerSelectedAt;
    private LocalDateTime paymentDeadline;
    private Integer gracePeriodDays;
    private BigDecimal penaltyAmount;
    private Integer paidMembersCount;
    private Integer totalMembersCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 