package com.mahiberawi.dto.payment;

import lombok.Data;

@Data
public class TeleBirrPaymentResponse {
    private String status;
    private String message;
    private String qrCode;
    private String paymentUrl;
    private String transactionId;
} 