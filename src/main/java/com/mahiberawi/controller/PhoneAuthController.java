package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.dto.auth.PhoneRegisterRequest;
import com.mahiberawi.dto.auth.PhoneVerificationRequest;
import com.mahiberawi.dto.auth.PhoneLoginRequest;
import com.mahiberawi.dto.auth.AuthResponse;
import com.mahiberawi.service.PhoneService;
import com.mahiberawi.service.AuthService;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserStatus;
import com.mahiberawi.repository.UserRepository;
import com.mahiberawi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class PhoneAuthController {
    private final PhoneService phoneService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/phone/register")
    public ResponseEntity<ApiResponse> registerByPhone(@Valid @RequestBody PhoneRegisterRequest request) {
        // Check if phone already exists
        if (userRepository.findByPhone(request.getPhoneNumber()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Phone number already registered")
                    .build());
        }
        // Optionally check for email uniqueness if provided
        if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Email already registered")
                    .build());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Passwords do not match")
                    .build());
        }
        // Create user (phone only, email optional)
        User user = User.builder()
                .phone(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword()) // Password should be encoded in real implementation
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .isPhoneVerified(false)
                .isEmailVerified(false)
                .build();
        userRepository.save(user);
        // Send verification SMS
        boolean smsSent = phoneService.sendVerificationSms(request.getPhoneNumber(), user.getFullName());
        if (smsSent) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Registration successful. Please verify your phone number.")
                    .build());
        } else {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Registration successful, but failed to send verification SMS.")
                    .build());
        }
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse> verifyPhone(@Valid @RequestBody PhoneVerificationRequest request) {
        // Verify the code
        boolean verified = phoneService.verifyPhoneCode(request.getPhoneNumber(), request.getCode());
        if (!verified) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Invalid or expired verification code")
                    .build());
        }
        // Mark user as phone verified
        var userOpt = userRepository.findByPhone(request.getPhoneNumber());
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            user.setPhoneVerified(true);
            userRepository.save(user);
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Phone verified successfully")
                .build());
    }

    @PostMapping("/phone/login")
    public ResponseEntity<AuthResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        // Find user by phone number
        var userOpt = userRepository.findByPhone(request.getPhoneNumber());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid phone number or password");
        }
        
        var user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid phone number or password");
        }
        
        // Generate JWT tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        
        var jwtToken = jwtService.generateToken(claims, user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
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
                .build());
    }

    @PostMapping("/phone/forgot-password")
    public ResponseEntity<ApiResponse> forgotPasswordByPhone(@RequestParam String phoneNumber) {
        log.info("Forgot password request for phone: {}", phoneNumber);
        
        // Check if user exists (but don't reveal if they do or don't)
        var userOptional = userRepository.findByPhone(phoneNumber);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean smsSent = phoneService.sendPasswordResetSms(phoneNumber, user.getFullName());
            
            if (smsSent) {
                log.info("Password reset SMS sent successfully to: {}", phoneNumber);
            } else {
                log.error("Failed to send password reset SMS to: {}", phoneNumber);
            }
        } else {
            log.warn("Forgot password request for non-existent phone: {}", phoneNumber);
        }
        
        // Always return success to prevent phone number enumeration
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("If the phone number exists, a reset SMS has been sent")
                .build());
    }

    @PostMapping("/phone/test-sms")
    public ResponseEntity<ApiResponse> testSms(@RequestParam String phoneNumber) {
        log.info("Testing SMS sending to: {}", phoneNumber);
        
        boolean smsSent = phoneService.sendTestSms(phoneNumber);
        
        if (smsSent) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Test SMS sent successfully to " + phoneNumber)
                    .build());
        } else {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Failed to send test SMS to " + phoneNumber)
                    .build());
        }
    }
} 