package com.mahiberawi.repository;

import com.mahiberawi.entity.PhoneVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneVerificationCodeRepository extends JpaRepository<PhoneVerificationCode, String> {
    
    /**
     * Find a valid verification code for a phone number
     */
    Optional<PhoneVerificationCode> findByPhoneNumberAndCodeAndUsedFalseAndExpiresAtAfter(
            String phoneNumber, String code, LocalDateTime now);
    
    /**
     * Mark all codes for a phone number as used
     */
    @Modifying
    @Query("UPDATE PhoneVerificationCode p SET p.used = true WHERE p.phoneNumber = :phoneNumber")
    void markAllCodesAsUsedForPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    /**
     * Delete expired verification codes
     */
    @Modifying
    @Query("DELETE FROM PhoneVerificationCode p WHERE p.expiresAt < :now")
    int deleteExpiredCodes(@Param("now") LocalDateTime now);
    
    /**
     * Find the most recent unused code for a phone number
     */
    Optional<PhoneVerificationCode> findFirstByPhoneNumberAndUsedFalseOrderByCreatedAtDesc(String phoneNumber);
    
    /**
     * Find all verification codes for a phone number
     */
    List<PhoneVerificationCode> findByPhoneNumber(String phoneNumber);
} 