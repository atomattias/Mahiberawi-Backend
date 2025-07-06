package com.mahiberawi.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VippsPaymentRequest {
    private String merchantInfo;
    private String customerInfo;
    private String transaction;
    private String paymentMethod;
    private String fallback;
    private String callbacks;
} 