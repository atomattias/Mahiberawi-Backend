package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.service.UserService;
import com.mahiberawi.service.GroupService;
import com.mahiberawi.service.EventService;
import com.mahiberawi.service.PaymentService;
import com.mahiberawi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;
    private final GroupService groupService;
    private final EventService eventService;
    private final PaymentService paymentService;
    
    // ========== ADMIN DASHBOARD ENDPOINTS ==========
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getAdminStats(@AuthenticationPrincipal User currentUser) {
        // Check if user is super admin
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access admin statistics");
        }
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // User statistics
            stats.put("totalUsers", userService.getAllUsers().size());
            stats.put("superAdmins", userService.getUsersByRole(UserRole.SUPER_ADMIN).size());
            stats.put("admins", userService.getUsersByRole(UserRole.ADMIN).size());
            stats.put("members", userService.getUsersByRole(UserRole.MEMBER).size());
            
            // Group statistics
            stats.put("totalGroups", groupService.getAllGroups().size());
            stats.put("activeGroups", groupService.getActiveGroups().size());
            
            // Event statistics
            stats.put("totalEvents", eventService.getAllEvents().size());
            stats.put("upcomingEvents", eventService.getUpcomingEvents().size());
            
            // Payment statistics
            stats.put("totalPayments", paymentService.getAllPayments().size());
            stats.put("completedPayments", paymentService.getCompletedPayments().size());
            
            log.info("Admin stats retrieved for user: {}", currentUser.getEmail());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Admin statistics retrieved successfully")
                    .data(stats)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving admin stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve admin statistics: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getAdminDashboard(@AuthenticationPrincipal User currentUser) {
        // Check if user is super admin
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access admin dashboard");
        }
        
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Recent activity
            dashboard.put("recentUsers", userService.getRecentUsers(10));
            dashboard.put("recentGroups", groupService.getRecentGroups(10));
            dashboard.put("recentEvents", eventService.getRecentEvents(10));
            
            // System health
            dashboard.put("systemStatus", "healthy");
            dashboard.put("databaseStatus", "connected");
            
            log.info("Admin dashboard retrieved for user: {}", currentUser.getEmail());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Admin dashboard retrieved successfully")
                    .data(dashboard)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving admin dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve admin dashboard: " + e.getMessage())
                    .build());
        }
    }
    
    // ========== USER MANAGEMENT ENDPOINTS ==========
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsersForAdmin(@AuthenticationPrincipal User currentUser) {
        // Check if user is super admin
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access user management");
        }
        
        try {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Users retrieved successfully")
                    .data(userService.getAllUsers())
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving users for admin: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve users: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/users/stats")
    public ResponseEntity<ApiResponse> getUserStats(@AuthenticationPrincipal User currentUser) {
        // Check if user is super admin
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access user statistics");
        }
        
        try {
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("totalUsers", userService.getAllUsers().size());
            userStats.put("verifiedUsers", userService.getVerifiedUsers().size());
            userStats.put("unverifiedUsers", userService.getUnverifiedUsers().size());
            userStats.put("activeUsers", userService.getActiveUsers().size());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("User statistics retrieved successfully")
                    .data(userStats)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving user stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve user statistics: " + e.getMessage())
                    .build());
        }
    }
    

        @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody com.mahiberawi.dto.user.UpdateUserRoleRequest request,
            @AuthenticationPrincipal User currentUser) {
        // Check if user is super admin
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can update user roles");
        }
        
        try {
            UserResponse updatedUser = userService.updateUserRole(userId, request.getRole());
            
            log.info("User role updated by super admin {}: user {} -> role {}", 
                    currentUser.getEmail(), userId, request.getRole());
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(updatedUser)
                    .message("User role updated successfully")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error updating user role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("Failed to update user role: " + e.getMessage())
                    .build());
        }
    }
    
    // ========== TEMPORARY PROMOTION ENDPOINTS ==========
    
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