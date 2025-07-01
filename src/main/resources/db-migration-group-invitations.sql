-- Migration for Group Invitations System
-- This migration adds support for enhanced group invitation management

-- Create group_invitations table if it doesn't exist
CREATE TABLE IF NOT EXISTS group_invitations (
    id VARCHAR(255) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    invitation_code VARCHAR(255) UNIQUE,
    invited_by VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_group_invitations_group_id (group_id),
    INDEX idx_group_invitations_email (email),
    INDEX idx_group_invitations_phone (phone),
    INDEX idx_group_invitations_invitation_code (invitation_code),
    INDEX idx_group_invitations_status (status),
    INDEX idx_group_invitations_expires_at (expires_at),
    INDEX idx_group_invitations_invited_by (invited_by)
);

-- Add message column to group_invitations if it doesn't exist
ALTER TABLE group_invitations 
ADD COLUMN IF NOT EXISTS message TEXT;

-- Add invitation_code column to group_invitations if it doesn't exist
ALTER TABLE group_invitations 
ADD COLUMN IF NOT EXISTS invitation_code VARCHAR(255) UNIQUE;

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_group_invitations_email_group ON group_invitations(email, group_id);
CREATE INDEX IF NOT EXISTS idx_group_invitations_phone_group ON group_invitations(phone, group_id);
CREATE INDEX IF NOT EXISTS idx_group_invitations_invitation_code ON group_invitations(invitation_code);
CREATE INDEX IF NOT EXISTS idx_group_invitations_expired ON group_invitations(expires_at, status) 
WHERE status = 'PENDING' AND expires_at < NOW();

-- Insert sample data for testing (optional)
-- INSERT INTO group_invitations (id, group_id, email, invited_by, status, expires_at, message) 
-- VALUES (UUID(), 'sample-group-id', 'test@example.com', 'sample-user-id', 'PENDING', DATE_ADD(NOW(), INTERVAL 24 HOUR), 'Welcome to our group!'); 