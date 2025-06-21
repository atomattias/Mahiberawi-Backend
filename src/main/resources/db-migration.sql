-- Migration script to add missing columns and update existing data
-- Run this script on your PostgreSQL database

-- Add the intention column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS intention VARCHAR(20) DEFAULT 'UNDECIDED';

-- Update existing users to have the correct role values
-- Convert old USER role to MEMBER
UPDATE users SET role = 'MEMBER' WHERE role = 'USER';

-- Update existing users to have the correct intention based on their role
-- This is a default mapping - you may want to adjust based on your business logic
UPDATE users SET intention = 'JOIN_ONLY' WHERE role = 'MEMBER' AND intention = 'UNDECIDED';
UPDATE users SET intention = 'CREATE_GROUPS' WHERE role IN ('ADMIN', 'SUPER_ADMIN') AND intention = 'UNDECIDED';

-- Add NOT NULL constraint after setting default values
ALTER TABLE users ALTER COLUMN intention SET NOT NULL;

-- Verify the changes
SELECT id, email, role, intention FROM users LIMIT 5; 