package com.mahiberawi.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeleBirrPaymentRequest {
    private String outTradeNo;
    private String totalAmount;
    private String subject;
    private String notifyUrl;
    private String returnUrl;
} 