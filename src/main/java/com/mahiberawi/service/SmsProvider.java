package com.mahiberawi.service;

/**
 * Interface for SMS providers to support multiple SMS services
 */
public interface SmsProvider {
    
    /**
     * Send SMS message to a phone number
     * 
     * @param phoneNumber The phone number to send SMS to (in international format)
     * @param message The message content to send
     * @return true if SMS was sent successfully, false otherwise
     */
    boolean sendSms(String phoneNumber, String message);
    
    /**
     * Send verification code to a phone number
     * 
     * @param phoneNumber The phone number to send verification code to
     * @param code The verification code to send
     * @return true if verification code was sent successfully, false otherwise
     */
    boolean sendVerificationCode(String phoneNumber, String code);
    
    /**
     * Verify a code sent to a phone number
     * 
     * @param phoneNumber The phone number the code was sent to
     * @param code The verification code to check
     * @return true if code is valid, false otherwise
     */
    boolean verifyCode(String phoneNumber, String code);
    
    /**
     * Get the name of this SMS provider
     * 
     * @return Provider name
     */
    String getProviderName();
    
    /**
     * Check if this provider supports the given phone number
     * 
     * @param phoneNumber The phone number to check
     * @return true if this provider supports the phone number
     */
    boolean supportsPhoneNumber(String phoneNumber);
} 