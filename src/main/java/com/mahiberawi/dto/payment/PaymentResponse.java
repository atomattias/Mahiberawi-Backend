package com.mahiberawi.dto.payment;

import com.mahiberawi.entity.PaymentMethod;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.entity.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String id;
    private String userId;
    private String userFullName;
    private String userEmail;
    private BigDecimal amount;
    private PaymentType type;
    private PaymentMethod method;
    private PaymentStatus status;
    private String eventId;
    private String eventName;
    private String description;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 