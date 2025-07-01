-- Migration for Email Verification Codes System
-- This migration adds the email_verification_codes table for email verification functionality

-- Create email_verification_codes table if it doesn't exist
CREATE TABLE IF NOT EXISTS email_verification_codes (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for better performance
    INDEX idx_email_verification_codes_email (email),
    INDEX idx_email_verification_codes_code (code),
    INDEX idx_email_verification_codes_expires_at (expires_at),
    INDEX idx_email_verification_codes_used (used),
    INDEX idx_email_verification_codes_email_used (email, used),
    INDEX idx_email_verification_codes_expired_unused (expires_at, used) 
    WHERE used = FALSE AND expires_at < NOW()
);

-- Add unique constraint to prevent duplicate codes for the same email
ALTER TABLE email_verification_codes 
ADD CONSTRAINT uk_email_verification_codes_email_code 
UNIQUE (email, code);

-- Verify the table was created
SELECT COUNT(*) as table_count 
FROM information_schema.tables 
WHERE table_name = 'email_verification_codes'; 