-- Database Migration: Update EQUB to ROSA
-- This script updates existing EQUB groups to use the new ROSA enum value

-- Update existing groups that have type 'EQUB' to 'ROSA'
UPDATE groups 
SET type = 'ROSA' 
WHERE type = 'EQUB';

-- Verify the update
SELECT id, name, type, created_at 
FROM groups 
WHERE type = 'ROSA' 
ORDER BY created_at DESC;

-- Show summary of changes
SELECT 
    'Groups updated from EQUB to ROSA' as action,
    COUNT(*) as count
FROM groups 
WHERE type = 'ROSA';
