package com.mahiberawi.service;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.dto.auth.AuthResponse;
import com.mahiberawi.dto.auth.LoginRequest;
import com.mahiberawi.dto.auth.RegisterRequest;
import com.mahiberawi.dto.auth.EmailVerificationRequest;
import com.mahiberawi.dto.auth.PhoneVerificationRequest;
import com.mahiberawi.dto.auth.ForgotPasswordRequest;
import com.mahiberawi.dto.auth.ResetPasswordRequest;
import com.mahiberawi.dto.auth.RegistrationResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserStatus;
import com.mahiberawi.entity.UserIntention;
import com.mahiberawi.repository.UserRepository;
import com.mahiberawi.repository.EmailVerificationCodeRepository;
import com.mahiberawi.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        log.info("Starting registration process for email: {}", request.getEmail());

        Optional<User> existingUserOptional = userRepository.findByEmail(request.getEmail());
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            if (existingUser.isEmailVerified()) {
                log.warn("Registration failed: Email already exists and is verified: {}", request.getEmail());
                throw new RuntimeException("Email already exists");
            } else {
                log.info("Unverified user attempting to register: {}. Resending verification email.", request.getEmail());
                boolean emailSent = emailService.resendVerificationEmail(existingUser.getEmail(), existingUser.getFullName());
                
                return RegistrationResponse.builder()
                        .success(false)
                        .message("Email is already registered but not verified. A new verification email has been sent.")
                        .requiresVerification(true)
                        .email(existingUser.getEmail())
                        .build();
            }
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration failed: Passwords do not match for email: {}", request.getEmail());
            throw new RuntimeException("Passwords do not match");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.MEMBER)
                .intention(UserIntention.UNDECIDED)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());

        // Send verification email
        boolean emailSent = emailService.sendVerificationEmail(user.getEmail(), user.getFullName());
        if (emailSent) {
            log.info("Verification email sent successfully to: {}", user.getEmail());
        } else {
            log.error("Failed to send verification email to: {}", user.getEmail());
            // Note: We don't fail registration if email fails, but we log it
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return RegistrationResponse.builder()
                .success(true)
                .message("Registration successful. Please check your email for verification.")
                .requiresVerification(true)
                .email(user.getEmail())
                .authResponse(AuthResponse.builder()
                        .token(jwtToken)
                        .refreshToken(refreshToken)
                        .user(com.mahiberawi.dto.UserResponse.builder()
                                .id(user.getId())
                                .name(user.getFullName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .intention(user.getIntention())
                                .isEmailVerified(user.isEmailVerified())
                                .isPhoneVerified(user.isPhoneVerified())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build())
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Login successful for user: {}", user.getId());

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .user(com.mahiberawi.dto.UserResponse.builder()
                        .id(user.getId())
                        .name(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .intention(user.getIntention())
                        .isEmailVerified(user.isEmailVerified())
                        .isPhoneVerified(user.isPhoneVerified())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .build();
    }

    @Transactional
    public AuthResponse verifyEmail(EmailVerificationRequest request) {
        log.info("Email verification attempt for: {}", request.getEmail());
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean isValidCode = emailService.verifyEmailCode(request.getEmail(), request.getCode());
        
        if (isValidCode) {
            user.setEmailVerified(true);
            userRepository.save(user);
            log.info("Email verified successfully for user: {}", user.getId());
            
            // Generate tokens for the verified user
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            
            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .user(com.mahiberawi.dto.UserResponse.builder()
                            .id(user.getId())
                            .name(user.getFullName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .intention(user.getIntention())
                            .isEmailVerified(user.isEmailVerified())
                            .isPhoneVerified(user.isPhoneVerified())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build())
                    .build();
        } else {
            log.warn("Invalid verification code for email: {}", request.getEmail());
            throw new RuntimeException("Invalid or expired verification code");
        }
    }

    @Transactional
    public ApiResponse resendVerificationEmail(String email) {
        log.info("Resend verification email request for: {}", email);
        
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean emailSent = emailService.resendVerificationEmail(email, user.getFullName());
        
        if (emailSent) {
            log.info("Verification email resent successfully to: {}", email);
            return ApiResponse.builder()
                    .success(true)
                    .message("Verification email sent successfully")
                    .build();
        } else {
            log.error("Failed to resend verification email to: {}", email);
            return ApiResponse.builder()
                    .success(false)
                    .message("Failed to send verification email")
                    .build();
        }
    }

    @Transactional
    public ApiResponse verifyPhone(PhoneVerificationRequest request) {
        // TODO: Implement phone verification logic
        // This would typically involve:
        // 1. Checking if the verification code is valid for the phone
        // 2. Updating the user's phone verification status
        // 3. Returning success/failure response
        
        var user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // For now, we'll just mark as verified (in real app, verify the code)
        user.setPhoneVerified(true);
        userRepository.save(user);
        
        return ApiResponse.builder()
                .success(true)
                .message("Phone verified successfully")
                .build();
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        
        // Check if user exists (but don't reveal if they do or don't)
        var userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean emailSent = emailService.sendPasswordResetEmail(request.getEmail(), user.getFullName());
            
            if (emailSent) {
                log.info("Password reset email sent successfully to: {}", request.getEmail());
            } else {
                log.error("Failed to send password reset email to: {}", request.getEmail());
            }
        } else {
            log.warn("Forgot password request for non-existent email: {}", request.getEmail());
        }
        
        // Always return success to prevent email enumeration
        return ApiResponse.builder()
                .success(true)
                .message("If the email exists, a reset link has been sent")
                .build();
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt for email: {}", request.getEmail());
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify the reset code
        boolean isValidCode = emailService.verifyEmailCode(request.getEmail(), request.getCode());
        
        if (isValidCode) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            log.info("Password reset successful for user: {}", user.getId());
            
            return ApiResponse.builder()
                    .success(true)
                    .message("Password reset successfully")
                    .build();
        } else {
            log.warn("Invalid reset code for email: {}", request.getEmail());
            return ApiResponse.builder()
                    .success(false)
                    .message("Invalid or expired reset code")
                    .build();
        }
    }

    /**
     * Test email configuration (for debugging)
     */
    public boolean testEmailConfiguration(String email) {
        log.info("Testing email configuration for: {}", email);
        return emailService.sendTestEmail(email);
    }

    /**
     * Delete user by email (for testing purposes)
     */
    @Transactional
    public boolean deleteUserByEmail(String email) {
        log.info("Deleting user with email: {}", email);
        
        try {
            // Find the user first
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user == null) {
                log.warn("User not found: {}", email);
                return false;
            }
            
            // Delete verification codes first
            emailVerificationCodeRepository.markAllCodesAsUsedForEmail(email);
            
            // Delete the user
            userRepository.delete(user);
            log.info("User deleted successfully: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting user: {}", email, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
} 