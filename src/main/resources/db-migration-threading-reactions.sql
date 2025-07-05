-- Migration for Threading and Reaction Features
-- This migration adds support for post threading and reactions

-- 1. Add parent_post_id column to posts table for threading
ALTER TABLE posts ADD COLUMN IF NOT EXISTS parent_post_id VARCHAR(255);
ALTER TABLE posts ADD CONSTRAINT IF NOT EXISTS fk_posts_parent_post 
    FOREIGN KEY (parent_post_id) REFERENCES posts(id) ON DELETE CASCADE;

-- 2. Create post_reactions table for reactions
CREATE TABLE IF NOT EXISTS post_reactions (
    id VARCHAR(255) PRIMARY KEY,
    post_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    reaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate reactions from same user
    UNIQUE KEY unique_user_post_reaction (user_id, post_id, reaction_type),
    
    -- Indexes for better performance
    INDEX idx_post_reactions_post_id (post_id),
    INDEX idx_post_reactions_user_id (user_id),
    INDEX idx_post_reactions_type (reaction_type),
    INDEX idx_post_reactions_post_user (post_id, user_id)
);

-- 3. Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_posts_parent_post_id ON posts(parent_post_id);
CREATE INDEX IF NOT EXISTS idx_posts_group_id ON posts(group_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);

-- 4. Insert sample data for testing (optional)
-- INSERT INTO posts (id, content, sender_id, group_id, type, created_at) 
-- VALUES (UUID(), 'Sample post', 'user-id', 'group-id', 'GROUP', NOW());

-- INSERT INTO post_reactions (id, post_id, user_id, reaction_type, created_at)
-- VALUES (UUID(), 'post-id', 'user-id', 'like', NOW()); 