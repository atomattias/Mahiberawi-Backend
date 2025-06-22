package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;
    
    // TEMPORARY ENDPOINT - REMOVE IN PRODUCTION
    @PostMapping("/promote/{email}")
    public ResponseEntity<ApiResponse> promoteToSuperAdmin(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            user.setRole(UserRole.SUPER_ADMIN);
            userService.updateUserRole(user.getId(), UserRole.SUPER_ADMIN);
            
            log.info("User {} promoted to SUPER_ADMIN", email);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("User promoted to SUPER_ADMIN successfully")
                    .data("User: " + email + " is now SUPER_ADMIN")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error promoting user to SUPER_ADMIN: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to promote user: " + e.getMessage())
                    .build());
        }
    }
    
    // TEMPORARY ENDPOINT - REMOVE IN PRODUCTION
    @GetMapping("/check-role/{email}")
    public ResponseEntity<ApiResponse> checkUserRole(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("User role retrieved successfully")
                    .data("User: " + email + " has role: " + user.getRole())
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to get user role: " + e.getMessage())
                    .build());
        }
    }
} 