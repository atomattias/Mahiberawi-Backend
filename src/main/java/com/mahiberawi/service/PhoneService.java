package com.mahiberawi.service;

import com.mahiberawi.entity.PhoneVerificationCode;
import com.mahiberawi.repository.PhoneVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneService {
    
    private final PhoneVerificationCodeRepository phoneVerificationCodeRepository;
    private final SmsProviderFactory smsProviderFactory;
    
    @Value("${app.phone.verification.expiry-minutes:15}")
    private int verificationCodeExpiryMinutes;
    
    @Value("${app.phone.verification.code-length:6}")
    private int verificationCodeLength;

    /**
     * Send phone verification code to user
     */
    @Transactional
    public boolean sendVerificationSms(String phoneNumber, String userName) {
        try {
            log.info("Sending verification SMS to: {}", phoneNumber);
            
            // Get appropriate SMS provider
            SmsProvider smsProvider = smsProviderFactory.getSmsProvider(phoneNumber);
            if (smsProvider == null) {
                log.error("No SMS provider found for phone number: {}", phoneNumber);
                return false;
            }
            
            // Generate verification code
            String verificationCode = generateVerificationCode();
            log.debug("Generated verification code: {} for phone: {}", verificationCode, phoneNumber);
            
            // Save verification code to database
            PhoneVerificationCode codeEntity = PhoneVerificationCode.builder()
                    .phoneNumber(phoneNumber)
                    .code(verificationCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                    .used(false)
                    .build();
            
            phoneVerificationCodeRepository.save(codeEntity);
            log.info("Verification code saved for phone: {}", phoneNumber);
            
            // Build SMS message
            String message = buildVerificationSmsContent(userName, verificationCode);
            
            // Send SMS
            boolean smsSent = smsProvider.sendSms(phoneNumber, message);
            
            if (smsSent) {
                log.info("Verification SMS sent successfully to: {}", phoneNumber);
                return true;
            } else {
                log.error("Failed to send verification SMS to: {}", phoneNumber);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to send verification SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Send password reset SMS
     */
    @Transactional
    public boolean sendPasswordResetSms(String phoneNumber, String userName) {
        try {
            log.info("Sending password reset SMS to: {}", phoneNumber);
            
            // Get appropriate SMS provider
            SmsProvider smsProvider = smsProviderFactory.getSmsProvider(phoneNumber);
            if (smsProvider == null) {
                log.error("No SMS provider found for phone number: {}", phoneNumber);
                return false;
            }
            
            // Generate reset code
            String resetCode = generateVerificationCode();
            
            // Save reset code to database
            PhoneVerificationCode codeEntity = PhoneVerificationCode.builder()
                    .phoneNumber(phoneNumber)
                    .code(resetCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                    .used(false)
                    .build();
            
            phoneVerificationCodeRepository.save(codeEntity);
            log.info("Password reset code saved for phone: {}", phoneNumber);
            
            // Build SMS message
            String message = buildPasswordResetSmsContent(userName, resetCode);
            
            // Send SMS
            boolean smsSent = smsProvider.sendSms(phoneNumber, message);
            
            if (smsSent) {
                log.info("Password reset SMS sent successfully to: {}", phoneNumber);
                return true;
            } else {
                log.error("Failed to send password reset SMS to: {}", phoneNumber);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to send password reset SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Verify phone verification code
     */
    @Transactional
    public boolean verifyPhoneCode(String phoneNumber, String code) {
        try {
            log.info("Verifying phone code for: {}", phoneNumber);
            
            PhoneVerificationCode verificationCode = phoneVerificationCodeRepository
                    .findByPhoneNumberAndCodeAndUsedFalseAndExpiresAtAfter(phoneNumber, code, LocalDateTime.now())
                    .orElse(null);
            
            if (verificationCode != null) {
                // Mark all codes for this phone number as used
                phoneVerificationCodeRepository.markAllCodesAsUsedForPhoneNumber(phoneNumber);
                log.info("Phone verification successful for: {}", phoneNumber);
                return true;
            } else {
                log.warn("Invalid or expired verification code for phone: {}", phoneNumber);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error verifying phone code for: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Resend verification SMS
     */
    @Transactional
    public boolean resendVerificationSms(String phoneNumber, String userName) {
        try {
            log.info("Resending verification SMS to: {}", phoneNumber);
            
            // Delete any existing unused codes for this phone number
            phoneVerificationCodeRepository.markAllCodesAsUsedForPhoneNumber(phoneNumber);
            
            // Send new verification SMS
            return sendVerificationSms(phoneNumber, userName);
            
        } catch (Exception e) {
            log.error("Failed to resend verification SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Clean up expired verification codes (runs every hour)
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredCodes() {
        try {
            log.info("Cleaning up expired phone verification codes");
            int deletedCount = phoneVerificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
            log.info("Deleted {} expired phone verification codes", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up expired phone verification codes", e);
        }
    }

    /**
     * Send test SMS (for debugging SMS configuration)
     */
    public boolean sendTestSms(String phoneNumber) {
        try {
            log.info("Sending test SMS to: {}", phoneNumber);
            
            // Get appropriate SMS provider
            SmsProvider smsProvider = smsProviderFactory.getSmsProvider(phoneNumber);
            if (smsProvider == null) {
                log.error("No SMS provider found for phone number: {}", phoneNumber);
                return false;
            }
            
            String message = "This is a test SMS to verify SMS configuration is working properly.\n\nTimestamp: " + LocalDateTime.now();
            
            boolean smsSent = smsProvider.sendSms(phoneNumber, message);
            
            if (smsSent) {
                log.info("Test SMS sent successfully to: {}", phoneNumber);
                return true;
            } else {
                log.error("Failed to send test SMS to: {}", phoneNumber);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Failed to send test SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * Generate a random verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < verificationCodeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Build SMS verification content
     */
    private String buildVerificationSmsContent(String userName, String code) {
        return String.format(
            "Hello %s,\n\n" +
            "Thank you for registering with Dewel! Please use the following verification code to complete your registration:\n\n" +
            "Verification Code: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't create an account with us, please ignore this SMS.\n\n" +
            "Best regards,\n" +
            "The Dewel Team",
            userName, code, verificationCodeExpiryMinutes
        );
    }

    /**
     * Build password reset SMS content
     */
    private String buildPasswordResetSmsContent(String userName, String code) {
        return String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Please use the following reset code:\n\n" +
            "Reset Code: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't request a password reset, please ignore this SMS.\n\n" +
            "Best regards,\n" +
            "The Dewel Team",
            userName, code, verificationCodeExpiryMinutes
        );
    }
} 