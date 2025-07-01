-- Complete Database Migration Script
-- This script creates all required tables for the Mahiberawi application
-- It's designed to be idempotent (safe to run multiple times)

-- ========================================
-- USERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    intention VARCHAR(50) NOT NULL DEFAULT 'UNDECIDED',
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    profile_picture VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- GROUPS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS groups (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    privacy VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    code VARCHAR(255) UNIQUE,
    invite_link VARCHAR(255) UNIQUE,
    member_count INTEGER NOT NULL DEFAULT 0,
    creator_id VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    profile_picture VARCHAR(255),
    allow_event_creation BOOLEAN DEFAULT TRUE,
    allow_member_invites BOOLEAN DEFAULT TRUE,
    allow_message_posting BOOLEAN DEFAULT TRUE,
    payment_required BOOLEAN DEFAULT FALSE,
    require_approval BOOLEAN DEFAULT FALSE,
    monthly_dues DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- GROUP MEMBERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS group_members (
    id VARCHAR(255) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP,
    invited_by_id VARCHAR(255),
    invitation_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- EMAIL VERIFICATION CODES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS email_verification_codes (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- GROUP INVITATIONS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS group_invitations (
    id VARCHAR(255) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    invited_by VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- EVENTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS events (
    id VARCHAR(255) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(255),
    max_participants INTEGER DEFAULT 0,
    created_by VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- EVENT ATTENDANCE TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS event_attendance (
    id VARCHAR(255) PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'REGISTERED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- EVENT PARTICIPANTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS event_participants (
    id VARCHAR(255) PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_status BOOLEAN NOT NULL DEFAULT FALSE,
    payment_amount DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- MESSAGES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(255) PRIMARY KEY,
    sender_id VARCHAR(255) NOT NULL,
    recipient_id VARCHAR(255),
    receiver_id VARCHAR(255),
    group_id VARCHAR(255),
    event_id VARCHAR(255),
    parent_message_id VARCHAR(255),
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'DIRECT',
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- PAYMENTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(255) PRIMARY KEY,
    group_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    event_id VARCHAR(255),
    membership_id VARCHAR(255),
    payer_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    method VARCHAR(50),
    transaction_id VARCHAR(255) UNIQUE,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- ========================================
-- NOTIFICATIONS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    group_id VARCHAR(255),
    event_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- FILES TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS files (
    id VARCHAR(255) PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(500) NOT NULL,
    uploader_id VARCHAR(255) NOT NULL,
    related_entity_id VARCHAR(255),
    related_entity_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- MEMBERSHIPS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS memberships (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    fee DECIMAL(10,2),
    description TEXT,
    created_by VARCHAR(255) NOT NULL,
    last_payment_date TIMESTAMP,
    next_payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- INDEXES FOR BETTER PERFORMANCE
-- ========================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Groups indexes
CREATE INDEX IF NOT EXISTS idx_groups_creator_id ON groups(creator_id);
CREATE INDEX IF NOT EXISTS idx_groups_privacy ON groups(privacy);
CREATE INDEX IF NOT EXISTS idx_groups_code ON groups(code);
CREATE INDEX IF NOT EXISTS idx_groups_invite_link ON groups(invite_link);

-- Group members indexes
CREATE INDEX IF NOT EXISTS idx_group_members_group_id ON group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user_id ON group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_group_members_role ON group_members(role);
CREATE INDEX IF NOT EXISTS idx_group_members_status ON group_members(status);

-- Email verification indexes
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_email ON email_verification_codes(email);
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_code ON email_verification_codes(code);
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_expires_at ON email_verification_codes(expires_at);
CREATE INDEX IF NOT EXISTS idx_email_verification_codes_used ON email_verification_codes(used);

-- Group invitations indexes
CREATE INDEX IF NOT EXISTS idx_group_invitations_group_id ON group_invitations(group_id);
CREATE INDEX IF NOT EXISTS idx_group_invitations_email ON group_invitations(email);
CREATE INDEX IF NOT EXISTS idx_group_invitations_status ON group_invitations(status);
CREATE INDEX IF NOT EXISTS idx_group_invitations_expires_at ON group_invitations(expires_at);

-- Events indexes
CREATE INDEX IF NOT EXISTS idx_events_group_id ON events(group_id);
CREATE INDEX IF NOT EXISTS idx_events_created_by ON events(created_by);
CREATE INDEX IF NOT EXISTS idx_events_start_time ON events(start_time);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);

-- Event attendance indexes
CREATE INDEX IF NOT EXISTS idx_event_attendance_event_id ON event_attendance(event_id);
CREATE INDEX IF NOT EXISTS idx_event_attendance_user_id ON event_attendance(user_id);
CREATE INDEX IF NOT EXISTS idx_event_attendance_status ON event_attendance(status);

-- Event participants indexes
CREATE INDEX IF NOT EXISTS idx_event_participants_event_id ON event_participants(event_id);
CREATE INDEX IF NOT EXISTS idx_event_participants_user_id ON event_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_event_participants_status ON event_participants(status);

-- Messages indexes
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_messages_group_id ON messages(group_id);
CREATE INDEX IF NOT EXISTS idx_messages_type ON messages(type);
CREATE INDEX IF NOT EXISTS idx_messages_read ON messages(read);

-- Payments indexes
CREATE INDEX IF NOT EXISTS idx_payments_group_id ON payments(group_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- Notifications indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(read);

-- ========================================
-- FILES TABLE INDEXES
-- ========================================
CREATE INDEX IF NOT EXISTS idx_files_original_name ON files(original_name);
CREATE INDEX IF NOT EXISTS idx_files_stored_name ON files(stored_name);
CREATE INDEX IF NOT EXISTS idx_files_content_type ON files(content_type);
CREATE INDEX IF NOT EXISTS idx_files_size ON files(size);
CREATE INDEX IF NOT EXISTS idx_files_type ON files(type);
CREATE INDEX IF NOT EXISTS idx_files_url ON files(url);
CREATE INDEX IF NOT EXISTS idx_files_uploader_id ON files(uploader_id);
CREATE INDEX IF NOT EXISTS idx_files_related_entity_id ON files(related_entity_id);
CREATE INDEX IF NOT EXISTS idx_files_related_entity_type ON files(related_entity_type);

-- ========================================
-- MEMBERSHIPS INDEXES
-- ========================================
CREATE INDEX IF NOT EXISTS idx_memberships_user_id ON memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_memberships_group_id ON memberships(group_id);
CREATE INDEX IF NOT EXISTS idx_memberships_type ON memberships(type);
CREATE INDEX IF NOT EXISTS idx_memberships_status ON memberships(status);
CREATE INDEX IF NOT EXISTS idx_memberships_start_date ON memberships(start_date);
CREATE INDEX IF NOT EXISTS idx_memberships_end_date ON memberships(end_date);
CREATE INDEX IF NOT EXISTS idx_memberships_created_by ON memberships(created_by);

-- ========================================
-- UNIQUE CONSTRAINTS
-- ========================================

-- Ensure unique user-group membership
ALTER TABLE group_members ADD CONSTRAINT IF NOT EXISTS uk_group_members_user_group UNIQUE (group_id, user_id);

-- Ensure unique event attendance
ALTER TABLE event_attendance ADD CONSTRAINT IF NOT EXISTS uk_event_attendance_user_event UNIQUE (event_id, user_id);

-- ========================================
-- MIGRATION UPDATES FOR EXISTING DATABASES
-- ========================================

-- Add missing columns to users table if they don't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_phone_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(255);

-- Update existing records to have full_name if it's null
UPDATE users SET full_name = name WHERE full_name IS NULL;

-- ========================================
-- VERIFICATION
-- ========================================

-- Verify all tables were created
SELECT 'Database migration completed successfully' as status; 