package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.service.UserCleanupService;
import com.mahiberawi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mahiberawi.entity.User;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative operations")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class AdminController {
    
    private final UserCleanupService userCleanupService;
    private final UserService userService;
    
    @Operation(
        summary = "Clean up expired unverified registrations",
        description = "Manually triggers cleanup of expired unverified user registrations"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Cleanup completed successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires admin privileges")
    })
    @PostMapping("/cleanup/expired-registrations")
    public ResponseEntity<ApiResponse> cleanupExpiredRegistrations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        
        log.info("Manual cleanup requested by admin: {}", user.getEmail());
        
        int deletedCount = userCleanupService.manualCleanup();
        
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cleanup completed successfully")
                .data(java.util.Map.of(
                    "deletedCount", deletedCount,
                    "message", "Deleted " + deletedCount + " expired unverified registrations"
                ))
                .build());
    }

    @Operation(
        summary = "Secure promotion to Super Admin",
        description = "Promotes a user to Super Admin role using environment variable-based security"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User promoted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid promotion key or user not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Super admin promotion not enabled")
    })
    @PostMapping("/secure-promote-super-admin/{email}")
    public ResponseEntity<ApiResponse> securePromoteToSuperAdmin(
            @Parameter(description = "Email of user to promote", required = true)
            @PathVariable String email,
            @Parameter(description = "Promotion key from environment variable", required = true)
            @RequestParam String promotionKey) {
        
        log.info("Secure promotion request for user: {}", email);
        
        try {
            boolean promoted = userService.securePromoteToSuperAdmin(email, promotionKey);
            
            if (promoted) {
                log.info("User {} successfully promoted to Super Admin", email);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("User promoted to Super Admin successfully")
                        .data(java.util.Map.of(
                            "email", email,
                            "role", "SUPER_ADMIN"
                        ))
                        .build());
            } else {
                log.warn("Failed to promote user {} to Super Admin", email);
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .success(false)
                        .message("Failed to promote user to Super Admin")
                        .build());
            }
            
        } catch (Exception e) {
            log.error("Error promoting user {} to Super Admin: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }
} 