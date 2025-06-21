package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import com.mahiberawi.dto.UserResponse;
import com.mahiberawi.dto.user.UpdateUserRoleRequest;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.service.UserService;
import com.mahiberawi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(@RequestParam(required = false) UserRole role) {
        // Check if user is super admin
        User currentUser = getCurrentUser();
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access user management");
        }

        List<UserResponse> users;
        if (role != null) {
            users = userService.getUsersByRole(role);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(users)
                    .message("Users filtered successfully")
                    .build());
        } else {
            users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .data(users)
                    .message("Users retrieved successfully")
                    .build());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchUsers(@RequestParam String q) {
        // Check if user is super admin
        User currentUser = getCurrentUser();
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can access user management");
        }

        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(users)
                .message("Search completed successfully")
                .build());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        // Check if user is super admin
        User currentUser = getCurrentUser();
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can update user roles");
        }

        UserResponse updatedUser = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(updatedUser)
                .message("User role updated successfully")
                .build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId) {
        // Check if user is super admin
        User currentUser = getCurrentUser();
        if (!userService.isSuperAdmin(currentUser)) {
            throw new UnauthorizedException("Only super admins can delete users");
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("User deleted successfully")
                .build());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
} 