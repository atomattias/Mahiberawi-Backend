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
    
    // Getter methods for debugging
    public String getAccountSid() { return accountSid; }
    public String getAuthToken() { return authToken; }
    public String getFromNumber() { return fromNumber; }
    public String getServiceSid() { return serviceSid; }
    public boolean isEnabled() { return enabled; }
    
    @Override
    public boolean sendSms(String phoneNumber, String message) {
        log.info("Twilio SMS provider - enabled: {}, accountSid: {}, fromNumber: {}", 
                enabled, accountSid != null ? "SET" : "NOT SET", fromNumber != null ? "SET" : "NOT SET");
        log.info("Twilio config details - accountSid length: {}, authToken length: {}, fromNumber: '{}', serviceSid length: {}", 
                accountSid != null ? accountSid.length() : 0, 
                authToken != null ? authToken.length() : 0, 
                fromNumber != null ? fromNumber : "NULL", 
                serviceSid != null ? serviceSid.length() : 0);
        log.info("Attempting to send SMS to: '{}' with message length: {}", phoneNumber, message.length());
        
        if (!enabled) {
            log.warn("Twilio SMS provider is not enabled");
            return false;
        }
        
        if (!supportsPhoneNumber(phoneNumber)) {
            log.warn("Twilio does not support phone number: {}", phoneNumber);
            return false;
        }
        
        if (accountSid == null || accountSid.isEmpty()) {
            log.error("Twilio account SID is not set");
            return false;
        }
        
        if (authToken == null || authToken.isEmpty()) {
            log.error("Twilio auth token is not set");
            return false;
        }
        
        if (fromNumber == null || fromNumber.isEmpty()) {
            log.error("Twilio from number is not set");
            return false;
        }
        
        try {
            // Initialize Twilio with credentials
            log.info("Initializing Twilio with accountSid: {} (length: {}), fromNumber: '{}'", 
                accountSid != null ? accountSid.substring(0, Math.min(10, accountSid.length())) + "..." : "NULL",
                accountSid != null ? accountSid.length() : 0,
                fromNumber);
            
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
            
            log.info("Creating Twilio SMS message - To: '{}', From: '{}', Message length: {}", 
                phoneNumber, fromNumber, message.length());
            
            // Send SMS using Twilio API
            Message messageObj = Message.creator(
                new PhoneNumber(phoneNumber), 
                new PhoneNumber(fromNumber), 
                message
            ).create();
            
            log.info("Twilio SMS sent successfully to {}: {} (SID: {}, Status: {})", 
                phoneNumber, message, messageObj.getSid(), messageObj.getStatus());
            
            // Check if the message was created successfully
            if (messageObj.getSid() != null && !messageObj.getSid().isEmpty()) {
                log.info("SMS created successfully with SID: {}", messageObj.getSid());
                return true;
            } else {
                log.error("SMS creation failed - no SID returned");
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio to {}: {}", phoneNumber, e.getMessage());
            log.error("Twilio SMS error details - Exception type: {}, Full stack trace:", e.getClass().getSimpleName());
            log.error("Stack trace:", e);
            
            // Log additional Twilio-specific error information
            if (e.getMessage() != null) {
                log.error("Twilio error message: {}", e.getMessage());
            }
            if (e.getCause() != null) {
                log.error("Twilio error cause: {}", e.getCause().getMessage());
            }
            
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
            log.error("Twilio verification error details - Exception type: {}, Full stack trace:", e.getClass().getSimpleName());
            log.error("Stack trace:", e);
            
            // Log additional Twilio-specific error information
            if (e.getMessage() != null) {
                log.error("Twilio verification error message: {}", e.getMessage());
            }
            if (e.getCause() != null) {
                log.error("Twilio verification error cause: {}", e.getCause().getMessage());
            }
            
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
            log.error("Twilio verification check error details - Exception type: {}, Full stack trace:", e.getClass().getSimpleName());
            log.error("Stack trace:", e);
            
            // Log additional Twilio-specific error information
            if (e.getMessage() != null) {
                log.error("Twilio verification check error message: {}", e.getMessage());
            }
            if (e.getCause() != null) {
                log.error("Twilio verification check error cause: {}", e.getCause().getMessage());
            }
            
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
    
    // Getter methods for debugging
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getAccountSid() {
        return accountSid;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public String getFromNumber() {
        return fromNumber;
    }
    
    public String getServiceSid() {
        return serviceSid;
    }
} 