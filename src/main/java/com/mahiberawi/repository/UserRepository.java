package com.mahiberawi.repository;

import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    
    // Search methods
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);
    
    // Filter methods
    List<User> findByRole(UserRole role);
    
    // Admin methods
    List<User> findTop10ByOrderByCreatedAtDesc();
    List<User> findByIsEmailVerifiedTrue();
    List<User> findByIsEmailVerifiedFalse();
    List<User> findByStatus(UserStatus status);
} 