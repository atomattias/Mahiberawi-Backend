package com.mahiberawi.dto.payment;

import lombok.Data;

@Data
public class VippsPaymentResponse {
    private String orderId;
    private String redirectUrl;
    private String reference;
    private String status;
    private String message;
    private String transactionId;
} 