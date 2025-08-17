package com.mahiberawi.service;

import com.mahiberawi.entity.User;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupService {
    
    private final UserRepository userRepository;
    
    // Registration expiration time in hours
    private static final int REGISTRATION_EXPIRY_HOURS = 24;
    
    /**
     * Clean up expired unverified user registrations
     * Runs every day at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2:00 AM
    @Transactional
    public void cleanupExpiredRegistrations() {
        log.info("Starting cleanup of expired unverified user registrations");
        
        LocalDateTime expiryThreshold = LocalDateTime.now().minusHours(REGISTRATION_EXPIRY_HOURS);
        
        // Find all unverified users created more than 24 hours ago
        List<User> expiredUsers = userRepository.findByIsPhoneVerifiedFalseAndCreatedAtBefore(expiryThreshold);
        
        if (expiredUsers.isEmpty()) {
            log.info("No expired unverified users found");
            return;
        }
        
        log.info("Found {} expired unverified users to delete", expiredUsers.size());
        
        // Delete expired users
        for (User user : expiredUsers) {
            log.info("Deleting expired unverified user: {} (created: {})", 
                    user.getPhone(), user.getCreatedAt());
        }
        
        userRepository.deleteAll(expiredUsers);
        
        log.info("Successfully deleted {} expired unverified users", expiredUsers.size());
    }
    
    /**
     * Manual cleanup method that can be called via admin endpoint
     */
    @Transactional
    public int manualCleanup() {
        log.info("Manual cleanup of expired unverified user registrations");
        
        LocalDateTime expiryThreshold = LocalDateTime.now().minusHours(REGISTRATION_EXPIRY_HOURS);
        List<User> expiredUsers = userRepository.findByIsPhoneVerifiedFalseAndCreatedAtBefore(expiryThreshold);
        
        if (!expiredUsers.isEmpty()) {
            userRepository.deleteAll(expiredUsers);
            log.info("Manual cleanup: Deleted {} expired unverified users", expiredUsers.size());
        }
        
        return expiredUsers.size();
    }
}
