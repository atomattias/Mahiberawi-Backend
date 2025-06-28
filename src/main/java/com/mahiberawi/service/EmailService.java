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
            "Thank you for registering with Dewel! Please use the following verification code to complete your registration:\n\n" +
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

    /**
     * Send group invitation email
     */
    @Transactional
    public boolean sendGroupInvitationEmail(String email, String groupCode, String inviterName) {
        try {
            log.info("Sending group invitation email to: {}", email);
            
            // Validate email configuration
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
                log.error("Email configuration not set properly. Please configure spring.mail.username in application.properties");
                return false;
            }
            
            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("You're Invited to Join a Group - Mahiberawi");
            
            String emailContent;
            if (groupCode != null && !groupCode.trim().isEmpty()) {
                emailContent = buildGroupInvitationEmailContent(inviterName, groupCode);
            } else {
                emailContent = buildGeneralGroupInvitationEmailContent(inviterName);
            }
            
            message.setText(emailContent);
            
            log.debug("Attempting to send group invitation email from: {} to: {}", fromEmail, email);
            mailSender.send(message);
            log.info("Group invitation email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send group invitation email to: {}", email, e);
            return false;
        }
    }

    /**
     * Send group invitation verification email
     */
    @Transactional
    public boolean sendGroupInvitationVerificationEmail(String email, String invitationToken, String userName) {
        try {
            log.info("Sending group invitation verification email to: {}", email);
            
            // Validate email configuration
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
                log.error("Email configuration not set properly. Please configure spring.mail.username in application.properties");
                return false;
            }
            
            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Verify Your Group Invitation - Mahiberawi");
            message.setText(buildGroupInvitationVerificationEmailContent(userName, invitationToken));
            
            log.debug("Attempting to send group invitation verification email from: {} to: {}", fromEmail, email);
            mailSender.send(message);
            log.info("Group invitation verification email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send group invitation verification email to: {}", email, e);
            return false;
        }
    }

    private String buildGroupInvitationEmailContent(String inviterName, String groupCode) {
        return String.format(
            "Hello!\n\n" +
            "%s has invited you to join a group on Mahiberawi.\n\n" +
            "To join the group, please use the following code in the Mahiberawi app:\n" +
            "Group Code: %s\n\n" +
            "If you don't have the Mahiberawi app, you can download it from your app store.\n\n" +
            "Best regards,\n" +
            "The Mahiberawi Team",
            inviterName, groupCode
        );
    }

    private String buildGeneralGroupInvitationEmailContent(String inviterName) {
        return String.format(
            "Hello!\n\n" +
            "%s has invited you to join Mahiberawi, a platform for managing groups and events.\n\n" +
            "To get started, please download the Mahiberawi app from your app store and create an account.\n\n" +
            "Best regards,\n" +
            "The Mahiberawi Team",
            inviterName
        );
    }

    private String buildGroupInvitationVerificationEmailContent(String userName, String invitationToken) {
        return String.format(
            "Hello %s!\n\n" +
            "You requested to join a group on Mahiberawi. To complete the process, please click the link below:\n\n" +
            "Verification Link: https://mahiberawi.com/verify-invitation?token=%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't request this invitation, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The Mahiberawi Team",
            userName, invitationToken
        );
    }

    /**
     * Send enhanced group invitation email with expiration and custom message
     */
    @Transactional
    public boolean sendEnhancedGroupInvitationEmail(String email, String groupName, String inviterName, 
                                                   String invitationCode, LocalDateTime expiresAt, String customMessage) {
        try {
            log.info("Sending enhanced group invitation email to: {}", email);
            
            // Validate email configuration
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com")) {
                log.error("Email configuration not set properly. Please configure spring.mail.username in application.properties");
                return false;
            }
            
            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("You're Invited to Join " + groupName + " - Mahiberawi");
            message.setText(buildEnhancedGroupInvitationEmailContent(inviterName, groupName, invitationCode, expiresAt, customMessage));
            
            log.debug("Attempting to send enhanced group invitation email from: {} to: {}", fromEmail, email);
            mailSender.send(message);
            log.info("Enhanced group invitation email sent successfully to: {}", email);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send enhanced group invitation email to: {}", email, e);
            return false;
        }
    }

    /**
     * Send SMS invitation (placeholder for SMS service integration)
     */
    @Transactional
    public boolean sendSMSInvitation(String phone, String groupName, String inviterName, 
                                   String invitationCode, LocalDateTime expiresAt, String customMessage) {
        try {
            log.info("Sending SMS invitation to: {}", phone);
            
            // TODO: Integrate with actual SMS service (Twilio, AWS SNS, etc.)
            // For now, just log the SMS content
            String smsContent = buildSMSInvitationContent(inviterName, groupName, invitationCode, expiresAt, customMessage);
            log.info("SMS content for {}: {}", phone, smsContent);
            
            // In a real implementation, you would send the SMS here
            // smsService.sendSMS(phone, smsContent);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send SMS invitation to: {}", phone, e);
            return false;
        }
    }

    /**
     * Generate unique invitation code
     */
    public String generateInvitationCode() {
        // Generate a unique 8-character alphanumeric code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }

    private String buildEnhancedGroupInvitationEmailContent(String inviterName, String groupName, 
                                                          String invitationCode, LocalDateTime expiresAt, String customMessage) {
        StringBuilder content = new StringBuilder();
        content.append("Hello!\n\n");
        content.append(inviterName).append(" has invited you to join the group \"").append(groupName).append("\" on Mahiberawi.\n\n");
        
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            content.append("Message from ").append(inviterName).append(": ").append(customMessage).append("\n\n");
        }
        
        content.append("To join the group, please use the following invitation code in the Mahiberawi app:\n");
        content.append("Invitation Code: ").append(invitationCode).append("\n\n");
        
        content.append("This invitation will expire on: ").append(expiresAt.toString()).append("\n\n");
        
        content.append("If you don't have the Mahiberawi app, you can download it from your app store.\n\n");
        content.append("Best regards,\nThe Mahiberawi Team");
        
        return content.toString();
    }

    private String buildSMSInvitationContent(String inviterName, String groupName, 
                                           String invitationCode, LocalDateTime expiresAt, String customMessage) {
        StringBuilder content = new StringBuilder();
        content.append(inviterName).append(" invited you to join \"").append(groupName).append("\" on Mahiberawi. ");
        content.append("Code: ").append(invitationCode).append(". ");
        content.append("Expires: ").append(expiresAt.toString()).append(". ");
        
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            content.append("Message: ").append(customMessage);
        }
        
        return content.toString();
    }
} 