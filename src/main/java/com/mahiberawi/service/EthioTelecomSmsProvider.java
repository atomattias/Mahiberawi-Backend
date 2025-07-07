package com.mahiberawi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Ethio Telecom SMS provider implementation for Ethiopian phone numbers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EthioTelecomSmsProvider implements SmsProvider {
    
    @Value("${sms.ethio-telecom.api-key:}")
    private String apiKey;
    
    @Value("${sms.ethio-telecom.api-url:}")
    private String apiUrl;
    
    @Value("${sms.ethio-telecom.enabled:false}")
    private boolean enabled;
    
    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!enabled) {
            log.warn("Ethio Telecom SMS provider is not enabled");
            return false;
        }
        
        if (!supportsPhoneNumber(phoneNumber)) {
            log.warn("Ethio Telecom does not support phone number: {}", phoneNumber);
            return false;
        }
        
        try {
            // TODO: Implement actual Ethio Telecom SMS sending
            // For now, we'll just log the SMS for testing
            log.info("Ethio Telecom SMS would be sent to {}: {}", phoneNumber, message);
            
            // Placeholder for actual Ethio Telecom implementation
            // This would involve making HTTP requests to Ethio Telecom's SMS gateway
            // with proper authentication and message formatting
            
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS via Ethio Telecom to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        if (!enabled) {
            log.warn("Ethio Telecom SMS provider is not enabled");
            return false;
        }
        
        if (!supportsPhoneNumber(phoneNumber)) {
            log.warn("Ethio Telecom does not support phone number: {}", phoneNumber);
            return false;
        }
        
        try {
            // TODO: Implement actual Ethio Telecom verification code sending
            log.info("Ethio Telecom verification code would be sent to {}: {}", phoneNumber, code);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification code via Ethio Telecom to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        if (!enabled) {
            log.warn("Ethio Telecom SMS provider is not enabled");
            return false;
        }
        
        try {
            // TODO: Implement actual Ethio Telecom code verification
            // For now, we'll just return true for testing
            log.info("Ethio Telecom verification check for {}: {}", phoneNumber, code);
            return true;
        } catch (Exception e) {
            log.error("Failed to verify code via Ethio Telecom for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "Ethio Telecom";
    }
    
    @Override
    public boolean supportsPhoneNumber(String phoneNumber) {
        // Ethio Telecom supports Ethiopian numbers (+251)
        return phoneNumber != null && phoneNumber.startsWith("+251");
    }
} 