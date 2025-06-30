package com.mahiberawi.controller;

import com.mahiberawi.dto.auth.AuthResponse;
import com.mahiberawi.dto.auth.LoginRequest;
import com.mahiberawi.dto.auth.RegisterRequest;
import com.mahiberawi.dto.auth.EmailVerificationRequest;
import com.mahiberawi.dto.auth.PhoneVerificationRequest;
import com.mahiberawi.dto.auth.ForgotPasswordRequest;
import com.mahiberawi.dto.auth.ResetPasswordRequest;
import com.mahiberawi.dto.auth.RegistrationResponse;
import com.mahiberawi.dto.UserResponse;
import com.mahiberawi.service.AuthService;
import com.mahiberawi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mahiberawi.entity.User;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns authentication tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = RegistrationResponse.class))
        ),
        @ApiResponse(
            responseCode = "202",
            description = "User exists but not verified, verification email sent"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists and verified"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegisterRequest request) {
        RegistrationResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (response.isRequiresVerification()) {
            return ResponseEntity.accepted().body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Login user",
        description = "Authenticates a user and returns JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "User login credentials", required = true)
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            AuthResponse response = authService.verifyEmail(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(com.mahiberawi.dto.ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> verifyPhone(@Valid @RequestBody PhoneVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyPhone(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @Operation(
        summary = "Resend verification email",
        description = "Resends email verification code to the user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification email sent successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> resendVerificationEmail(
            @Parameter(description = "Email address", required = true)
            @RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerificationEmail(email));
    }

    @Operation(
        summary = "Test email configuration",
        description = "Sends a test email to verify SMTP configuration (for debugging only)"
    )
    @PostMapping("/test-email")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> testEmail(
            @Parameter(description = "Email address to send test to", required = true)
            @RequestParam String email) {
        try {
            boolean sent = authService.testEmailConfiguration(email);
            if (sent) {
                return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                        .success(true)
                        .message("Test email sent successfully")
                        .build());
            } else {
                return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                        .success(false)
                        .message("Failed to send test email")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @Operation(
        summary = "Delete user (Admin only - for testing)",
        description = "Deletes a user and their verification codes (temporary endpoint for testing)"
    )
    @DeleteMapping("/delete-user")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> deleteUser(
            @Parameter(description = "Email address to delete", required = true)
            @RequestParam String email) {
        try {
            boolean deleted = authService.deleteUserByEmail(email);
            if (deleted) {
                return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                        .success(true)
                        .message("User deleted successfully")
                        .build());
            } else {
                return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                        .success(false)
                        .message("User not found")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile information of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        UserResponse userResponse = userService.getUserResponseById(user.getId());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
        summary = "Logout user",
        description = "Logs out the current user. Since JWT tokens are stateless, " +
                     "this endpoint returns success and the client should clear local tokens."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        // For JWT tokens, logout is handled client-side by clearing tokens
        // This endpoint provides a clean way to handle logout requests
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Logout successful")
                .build());
    }
} 