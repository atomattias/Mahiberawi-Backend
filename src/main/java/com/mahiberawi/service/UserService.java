package com.mahiberawi.service;

import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserStatus;
import com.mahiberawi.dto.UserResponse;
import com.mahiberawi.repository.UserRepository;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> searchUsers(String searchQuery) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchQuery, searchQuery, searchQuery);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(UserRole role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUserRole(String userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    public boolean isSuperAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    public UserResponse getUserResponseById(String userId) {
        User user = getUserById(userId);
        return convertToUserResponse(user);
    }

    private UserResponse convertToUserResponse(User user) {
        int groupCount = groupMemberRepository.countByUserId(user.getId());
        int createdGroups = groupRepository.countByCreatorId(user.getId());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .intention(user.getIntention())
                .isEmailVerified(user.isEmailVerified())
                .isPhoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .groupCount(groupCount)
                .createdGroups(createdGroups)
                .build();
    }

    // ========== ADMIN METHODS ==========
    
    public List<UserResponse> getRecentUsers(int limit) {
        List<User> users = userRepository.findTop10ByOrderByCreatedAtDesc();
        return users.stream()
                .limit(limit)
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getVerifiedUsers() {
        List<User> users = userRepository.findByIsEmailVerifiedTrue();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getUnverifiedUsers() {
        List<User> users = userRepository.findByIsEmailVerifiedFalse();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    public List<UserResponse> getActiveUsers() {
        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
} 