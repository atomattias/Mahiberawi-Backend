package com.mahiberawi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vipps")
public class VippsConfig {
    private String clientId;
    private String clientSecret;
    private String merchantId;
    private String subscriptionKey;
    private String baseUrl;
    private String paymentUrl;
    private String captureUrl;
    private String refundUrl;
    private String notifyUrl;
    private String returnUrl;
    private String cancelUrl;
    private String fallbackUrl;
} 