package com.mahiberawi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory to select the appropriate SMS provider based on phone number
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsProviderFactory {
    
    private final List<SmsProvider> smsProviders;
    
    /**
     * Get the appropriate SMS provider for the given phone number
     * 
     * @param phoneNumber The phone number to send SMS to
     * @return The appropriate SMS provider, or null if no provider supports the number
     */
    public SmsProvider getSmsProvider(String phoneNumber) {
        log.info("Original phone number received: '{}'", phoneNumber);
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.warn("Phone number is null or empty");
            return null;
        }
        
        // Normalize phone number to international format
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        log.info("Normalized phone number: '{}'", normalizedPhone);
        
        for (SmsProvider provider : smsProviders) {
            if (provider.supportsPhoneNumber(normalizedPhone)) {
                log.debug("Selected SMS provider {} for phone number {}", 
                    provider.getProviderName(), normalizedPhone);
                return provider;
            }
        }
        
        log.warn("No SMS provider found for phone number: {}", phoneNumber);
        return null;
    }
    
    /**
     * Normalize phone number to international format
     * 
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number in international format
     */
    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber.trim();
        
        // Remove any non-digit characters except +
        normalized = normalized.replaceAll("[^\\d+]", "");
        
        // Handle Ethiopian numbers
        if (normalized.startsWith("0")) {
            // Convert 0XXXXXXXXX to +251XXXXXXXXX
            normalized = "+251" + normalized.substring(1);
        } else if (normalized.startsWith("251")) {
            // Convert 251XXXXXXXXX to +251XXXXXXXXX
            normalized = "+" + normalized;
        } else if (!normalized.startsWith("+")) {
            // If no country code, assume Ethiopian
            normalized = "+251" + normalized;
        }
        
        return normalized;
    }
} 