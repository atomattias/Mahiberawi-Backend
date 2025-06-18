package com.mahiberawi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telebirr")
public class TeleBirrConfig {
    private String appKey;
    private String appSecret;
    private String tokenUrl;
    private String paymentUrl;
    private String verifyUrl;
    private String notifyUrl;
    private String returnUrl;
} 