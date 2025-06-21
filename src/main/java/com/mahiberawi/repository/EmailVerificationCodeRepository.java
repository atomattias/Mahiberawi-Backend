package com.mahiberawi.repository;

import com.mahiberawi.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, String> {
    
    Optional<EmailVerificationCode> findByEmailAndCodeAndUsedFalseAndExpiresAtAfter(
            String email, String code, LocalDateTime now);
    
    List<EmailVerificationCode> findByEmailAndUsedFalse(String email);
    
    @Modifying
    @Query("UPDATE EmailVerificationCode e SET e.used = true WHERE e.email = :email AND e.used = false")
    void markAllCodesAsUsedForEmail(@Param("email") String email);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationCode e WHERE e.expiresAt < :now")
    int deleteExpiredCodes(@Param("now") LocalDateTime now);
} 