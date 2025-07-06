package com.mahiberawi.dto.equb;

import com.mahiberawi.entity.enums.EqubSelectionMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EqubGroupRequest {
    @NotNull(message = "Equb amount is required")
    @Positive(message = "Equb amount must be positive")
    private BigDecimal equbAmount;

    @NotNull(message = "Selection method is required")
    private EqubSelectionMethod selectionMethod;

    @Positive(message = "Grace period days must be positive")
    private Integer gracePeriodDays = 7;

    @Positive(message = "Payment deadline days must be positive")
    private Integer paymentDeadlineDays = 15;

    private BigDecimal penaltyAmount;
} 