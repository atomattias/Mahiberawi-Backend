package com.mahiberawi.dto.payment;

import com.mahiberawi.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupPaymentRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Group ID is required")
    private String groupId;

    private String eventId;
    private String membershipId;
    
    // Optional fields for payment details
    private LocalDateTime dueDate;
    private List<String> targetUserIds; // If null, applies to all group members
    private String paymentInstructions;
    private Boolean isMandatory;
} 