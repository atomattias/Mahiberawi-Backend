package com.mahiberawi.service;

import com.mahiberawi.entity.EmailVerificationCode;
import com.mahiberawi.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.verification.expiry-minutes:15}")
    private int verificationCodeExpiryMinutes;
    
    @Value("${app.email.verification.code-length:6}")
    private int verificationCodeLength;

    /**
     * Send email verification code to user
     */
    @Transactional
    public boolean sendVerificationEmail(String email, String userName) {
        try {
            log.info("Sending verification email to: {}", email);
            
            // Validate email configuration
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
                log.error("Email configuration not set properly. Please configure spring.mail.username in application.properties");
                return false;
            }
            
            // Generate verification code
            String verificationCode = generateVerificationCode();
            log.debug("Generated verification code: {} for email: {}", verificationCode, email);
            
            // Save verification code to database
            EmailVerificationCode codeEntity = EmailVerificationCode.builder()
                    .email(email)
                    .code(verificationCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                    .used(false)
                    .build();
            
            emailVerificationCodeRepository.save(codeEntity);
            log.info("Verification code saved for email: {}", email);
            
            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Verify Your Email - Mahiberawi");
            message.setText(buildVerificationEmailContent(userName, verificationCode));
            
            log.debug("Attempting to send email from: {} to: {}", fromEmail, email);
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            log.error("Email configuration - Username: {}", fromEmail);
            return false;
        }
    }

    /**
     * Send password reset email
     */
    @Transactional
    public boolean sendPasswordResetEmail(String email, String userName) {
        try {
            log.info("Sending password reset email to: {}", email);
            
            // Generate reset code
            String resetCode = generateVerificationCode();
            
            // Save reset code to database
            EmailVerificationCode codeEntity = EmailVerificationCode.builder()
                    .email(email)
                    .code(resetCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                    .used(false)
                    .build();
            
            emailVerificationCodeRepository.save(codeEntity);
            log.info("Password reset code saved for email: {}", email);
            
            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Reset Your Password - Mahiberawi");
            message.setText(buildPasswordResetEmailContent(userName, resetCode));
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            return false;
        }
    }

    /**
     * Verify email verification code
     */
    @Transactional
    public boolean verifyEmailCode(String email, String code) {
        try {
            log.info("Verifying email code for: {}", email);
            
            EmailVerificationCode verificationCode = emailVerificationCodeRepository
                    .findByEmailAndCodeAndUsedFalseAndExpiresAtAfter(email, code, LocalDateTime.now())
                    .orElse(null);
            
            if (verificationCode != null) {
                // Mark all codes for this email as used
                emailVerificationCodeRepository.markAllCodesAsUsedForEmail(email);
                log.info("Email verification successful for: {}", email);
                return true;
            } else {
                log.warn("Invalid or expired verification code for email: {}", email);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error verifying email code for: {}", email, e);
            return false;
        }
    }

    /**
     * Resend verification email
     */
    @Transactional
    public boolean resendVerificationEmail(String email, String userName) {
        try {
            log.info("Resending verification email to: {}", email);
            
            // Delete any existing unused codes for this email
            emailVerificationCodeRepository.markAllCodesAsUsedForEmail(email);
            
            // Send new verification email
            return sendVerificationEmail(email, userName);
            
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", email, e);
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
            log.info("Cleaning up expired verification codes");
            int deletedCount = emailVerificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
            log.info("Deleted {} expired verification codes", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up expired verification codes", e);
        }
    }

    /**
     * Send test email (for debugging SMTP configuration)
     */
    public boolean sendTestEmail(String email) {
        try {
            log.info("Sending test email to: {}", email);
            
            // Validate email configuration
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
                log.error("Email configuration not set properly. Please configure spring.mail.username in application.properties");
                return false;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Test Email - Mahiberawi");
            message.setText("This is a test email to verify SMTP configuration is working properly.\n\nTimestamp: " + LocalDateTime.now());
            
            log.debug("Attempting to send test email from: {} to: {}", fromEmail, email);
            mailSender.send(message);
            log.info("Test email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", email, e);
            log.error("Email configuration - Username: {}", fromEmail);
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
     * Build email verification content
     */
    private String buildVerificationEmailContent(String userName, String code) {
        return String.format(
            "Hello %s,\n\n" +
            "Thank you for registering with Mahiberawi! Please use the following verification code to complete your registration:\n\n" +
            "Verification Code: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't create an account with us, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Mahiberawi Team",
            userName, code, verificationCodeExpiryMinutes
        );
    }

    /**
     * Build password reset email content
     */
    private String buildPasswordResetEmailContent(String userName, String code) {
        return String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Please use the following reset code:\n\n" +
            "Reset Code: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Mahiberawi Team",
            userName, code, verificationCodeExpiryMinutes
        );
    }
} 