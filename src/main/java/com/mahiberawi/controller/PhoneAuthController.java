package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.dto.auth.PhoneRegisterRequest;
import com.mahiberawi.dto.auth.PhoneVerificationRequest;
import com.mahiberawi.dto.auth.PhoneLoginRequest;
import com.mahiberawi.dto.auth.AuthResponse;
import com.mahiberawi.dto.auth.RegistrationStatusResponse;
import com.mahiberawi.dto.auth.ResendVerificationRequest;
import com.mahiberawi.service.PhoneService;
import com.mahiberawi.service.AuthService;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserStatus;
import com.mahiberawi.entity.UserIntention;
import com.mahiberawi.repository.UserRepository;
import com.mahiberawi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class PhoneAuthController {
    private final PhoneService phoneService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Registration expiration time in hours
    private static final int REGISTRATION_EXPIRY_HOURS = 24;

    @PostMapping("/phone/register")
    public ResponseEntity<ApiResponse> registerByPhone(@Valid @RequestBody PhoneRegisterRequest request) {
        log.info("Phone registration attempt for: {}", request.getPhoneNumber());
        
        // Check if phone already exists
        var existingUserOpt = userRepository.findByPhone(request.getPhoneNumber());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            
            if (existingUser.isPhoneVerified()) {
                // User is already verified - provide helpful message
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .success(false)
                        .message("Phone number already registered and verified. Please try logging in instead.")
                        .data(Map.of(
                            "status", "VERIFIED",
                            "suggestion", "LOGIN_INSTEAD"
                        ))
                        .build());
            } else {
                // User exists but not verified - check if registration has expired
                LocalDateTime expiryTime = existingUser.getCreatedAt().plusHours(REGISTRATION_EXPIRY_HOURS);
                boolean isExpired = LocalDateTime.now().isAfter(expiryTime);
                
                if (isExpired) {
                    // Registration expired - delete old user and allow new registration
                    log.info("Deleting expired unverified user: {}", request.getPhoneNumber());
                    userRepository.delete(existingUser);
                } else {
                    // Registration still valid - provide helpful message with resend option
                    long timeRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryTime);
                    return ResponseEntity.badRequest().body(ApiResponse.builder()
                            .success(false)
                            .message("Phone number already registered but not verified. You can resend the verification SMS.")
                            .data(Map.of(
                                "status", "PENDING_VERIFICATION",
                                "suggestion", "RESEND_VERIFICATION",
                                "timeRemaining", timeRemaining,
                                "canResend", true
                            ))
                            .build());
                }
            }
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
        // Generate placeholder email if not provided
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            // Generate a unique placeholder email for phone-only users
            email = "phone+" + request.getPhoneNumber().replaceAll("[^0-9]", "") + "@mahiberawi.com";
        }
        
        User user = User.builder()
                .phone(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword())) // Encode password properly
                .role(UserRole.MEMBER)
                .intention(UserIntention.UNDECIDED) // Set default intention
                .status(UserStatus.ACTIVE)
                .isPhoneVerified(false)
                .isEmailVerified(false)
                .build();
        userRepository.save(user);
        
        // Send verification SMS
        boolean smsSent = phoneService.sendVerificationSms(request.getPhoneNumber(), user.getFullName());
        if (smsSent) {
            log.info("Phone registration successful for: {}", request.getPhoneNumber());
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Registration successful. Please verify your phone number.")
                    .data(Map.of(
                        "phoneNumber", request.getPhoneNumber(),
                        "expiresAt", user.getCreatedAt().plusHours(REGISTRATION_EXPIRY_HOURS)
                    ))
                    .build());
        } else {
            log.warn("Phone registration successful but SMS failed for: {}", request.getPhoneNumber());
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Registration successful, but failed to send verification SMS. You can request a new verification code.")
                    .data(Map.of(
                        "phoneNumber", request.getPhoneNumber(),
                        "canResend", true
                    ))
                    .build());
        }
    }

    @GetMapping("/phone/registration-status/{phoneNumber}")
    public ResponseEntity<RegistrationStatusResponse> getRegistrationStatus(@PathVariable String phoneNumber) {
        log.info("Registration status check for: {}", phoneNumber);
        
        // Privacy-conscious approach: Don't reveal detailed status to unauthenticated users
        var userOpt = userRepository.findByPhone(phoneNumber);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(RegistrationStatusResponse.builder()
                    .success(false)
                    .status("NOT_FOUND")
                    .phoneNumber(phoneNumber)
                    .message("Phone number not found in our system")
                    .build());
        }
        
        // For unauthenticated requests, only reveal that the number is registered
        // Don't reveal verification status or other details
        return ResponseEntity.ok(RegistrationStatusResponse.builder()
                .success(true)
                .status("REGISTERED") // Generic status
                .phoneNumber(phoneNumber)
                .message("Phone number is registered in our system")
                .build());
    }

    // Alternative: Authenticated status check (more secure)
    @GetMapping("/phone/my-registration-status")
    public ResponseEntity<RegistrationStatusResponse> getMyRegistrationStatus(
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null || currentUser.getPhone() == null) {
            return ResponseEntity.status(401).body(RegistrationStatusResponse.builder()
                    .success(false)
                    .message("Authentication required")
                    .build());
        }
        
        log.info("Authenticated registration status check for: {}", currentUser.getPhone());
        
        if (currentUser.isPhoneVerified()) {
            return ResponseEntity.ok(RegistrationStatusResponse.builder()
                    .success(true)
                    .status("VERIFIED")
                    .phoneNumber(currentUser.getPhone())
                    .createdAt(currentUser.getCreatedAt())
                    .message("Your phone number is verified and active")
                    .build());
        }
        
        // Check if registration has expired
        LocalDateTime expiryTime = currentUser.getCreatedAt().plusHours(REGISTRATION_EXPIRY_HOURS);
        boolean isExpired = LocalDateTime.now().isAfter(expiryTime);
        
        if (isExpired) {
            return ResponseEntity.ok(RegistrationStatusResponse.builder()
                    .success(false)
                    .status("EXPIRED")
                    .phoneNumber(currentUser.getPhone())
                    .message("Your registration has expired. Please register again.")
                    .build());
        }
        
        long timeRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryTime);
        
        return ResponseEntity.ok(RegistrationStatusResponse.builder()
                .success(true)
                .status("PENDING_VERIFICATION")
                .phoneNumber(currentUser.getPhone())
                .createdAt(currentUser.getCreatedAt())
                .expiresAt(expiryTime)
                .timeRemaining(timeRemaining)
                .canResend(true)
                .message("Your registration is pending verification")
                .build());
    }

    @PostMapping("/phone/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return resendVerificationInternal(request);
    }

    @PostMapping("/send-phone-verification")
    public ResponseEntity<ApiResponse> sendPhoneVerification(@RequestBody Map<String, String> request) {
        log.info("Send phone verification request for: {}", request.get("phone"));
        
        ResendVerificationRequest resendRequest = ResendVerificationRequest.builder()
                .phoneNumber(request.get("phone"))
                .build();
        
        return resendVerificationInternal(resendRequest);
    }

    private ResponseEntity<ApiResponse> resendVerificationInternal(@Valid ResendVerificationRequest request) {
        log.info("Resend verification request for: {}", request.getPhoneNumber());
        
        var userOpt = userRepository.findByPhone(request.getPhoneNumber());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Phone number not found in our system")
                    .build());
        }
        
        User user = userOpt.get();
        
        if (user.isPhoneVerified()) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Phone number is already verified")
                    .build());
        }
        
        // Check if registration has expired
        LocalDateTime expiryTime = user.getCreatedAt().plusHours(REGISTRATION_EXPIRY_HOURS);
        boolean isExpired = LocalDateTime.now().isAfter(expiryTime);
        
        if (isExpired) {
            // Delete expired user
            log.info("Deleting expired unverified user: {}", request.getPhoneNumber());
            userRepository.delete(user);
            
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Registration has expired. Please register again.")
                    .build());
        }
        
        // Resend verification SMS
        boolean smsSent = phoneService.sendVerificationSms(request.getPhoneNumber(), user.getFullName());
        
        if (smsSent) {
            log.info("Verification SMS resent successfully to: {}", request.getPhoneNumber());
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Verification SMS sent successfully")
                    .data(Map.of(
                        "phoneNumber", request.getPhoneNumber(),
                        "expiresAt", expiryTime
                    ))
                    .build());
        } else {
            log.error("Failed to resend verification SMS to: {}", request.getPhoneNumber());
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Failed to send verification SMS. Please try again later.")
                    .build());
        }
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<ApiResponse> verifyPhone(@Valid @RequestBody PhoneVerificationRequest request) {
        log.info("Phone verification attempt for: {}", request.getPhoneNumber());
        
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
            log.info("Phone verified successfully for: {}", request.getPhoneNumber());
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

    @GetMapping("/phone/debug-codes")
    public ResponseEntity<ApiResponse> getDebugCodes(@RequestParam String phoneNumber) {
        // This endpoint should only be available in development
        if (!"development".equals(System.getProperty("spring.profiles.active"))) {
            return ResponseEntity.notFound().build();
        }
        
        var codes = phoneService.getVerificationCodesForPhone(phoneNumber);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Debug codes for " + phoneNumber)
                .data(codes)
                .build());
    }
} 