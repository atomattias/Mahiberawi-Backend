package com.mahiberawi.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Twilio SMS provider implementation for international phone numbers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioSmsProvider implements SmsProvider {
    
    @Value("${sms.twilio.account-sid:}")
    private String accountSid;
    
    @Value("${sms.twilio.auth-token:}")
    private String authToken;
    
    @Value("${sms.twilio.from-number:}")
    private String fromNumber;
    
    @Value("${sms.twilio.service-sid:}")
    private String serviceSid;
    
    @Value("${sms.twilio.enabled:false}")
    private boolean enabled;
    
    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!enabled) {
            log.warn("Twilio SMS provider is not enabled");
            return false;
        }
        
        if (!supportsPhoneNumber(phoneNumber)) {
            log.warn("Twilio does not support phone number: {}", phoneNumber);
            return false;
        }
        
        try {
            // Initialize Twilio with credentials
            Twilio.init(accountSid, authToken);
            
            // Send SMS using Twilio API
            Message.creator(
                new PhoneNumber(phoneNumber), 
                new PhoneNumber(fromNumber), 
                message
            ).create();
            
            log.info("Twilio SMS sent successfully to {}: {}", phoneNumber, message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        if (!enabled) {
            log.warn("Twilio SMS provider is not enabled");
            return false;
        }
        
        if (!supportsPhoneNumber(phoneNumber)) {
            log.warn("Twilio does not support phone number: {}", phoneNumber);
            return false;
        }
        
        try {
            // Initialize Twilio with credentials
            Twilio.init(accountSid, authToken);
            
            // Create verification using Twilio Verify API
            Verification verification = Verification.creator(
                    serviceSid,
                    phoneNumber,
                    "sms"
            ).create();
            
            log.info("Twilio verification sent to {} with status: {}", phoneNumber, verification.getStatus());
            return "pending".equals(verification.getStatus());
        } catch (Exception e) {
            log.error("Failed to send verification code via Twilio to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        if (!enabled) {
            log.warn("Twilio SMS provider is not enabled");
            return false;
        }
        
        try {
            // Initialize Twilio with credentials
            Twilio.init(accountSid, authToken);
            
            // Check verification using Twilio Verify API
            VerificationCheck verificationCheck = VerificationCheck.creator(serviceSid)
                .setTo(phoneNumber)
                .setCode(code)
                .create();
            
            log.info("Twilio verification check for {}: {}", phoneNumber, verificationCheck.getStatus());
            return "approved".equals(verificationCheck.getStatus());
        } catch (Exception e) {
            log.error("Failed to verify code via Twilio for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "Twilio";
    }
    
    @Override
    public boolean supportsPhoneNumber(String phoneNumber) {
        // Twilio supports international numbers, but we'll use it for non-Ethiopian numbers
        return phoneNumber != null && !phoneNumber.startsWith("+251");
    }
} 