package com.mahiberawi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationConfig {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Bean
    @Transactional
    public CommandLineRunner runMigration() {
        return args -> {
            log.info("Running database migration...");
            
            try {
                // Add the intention column if it doesn't exist (allow NULL initially)
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS intention VARCHAR(20)");
                log.info("Added intention column to users table");
                
                // Set default value for existing NULL intention values
                jdbcTemplate.update("UPDATE users SET intention = 'UNDECIDED' WHERE intention IS NULL");
                log.info("Set default intention values for existing users");
                
                // Drop the existing role check constraint if it exists
                try {
                    jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
                    log.info("Dropped existing role check constraint");
                } catch (Exception e) {
                    log.info("No existing role check constraint to drop: {}", e.getMessage());
                }
                
                // Update existing users to have the correct role values FIRST
                int updatedRoles = jdbcTemplate.update("UPDATE users SET role = 'MEMBER' WHERE role = 'USER'");
                log.info("Updated {} users from USER role to MEMBER", updatedRoles);
                
                // Update existing users to have the correct intention based on their role
                int updatedIntentions1 = jdbcTemplate.update("UPDATE users SET intention = 'JOIN_ONLY' WHERE role = 'MEMBER' AND intention = 'UNDECIDED'");
                log.info("Updated {} users with MEMBER role to JOIN_ONLY intention", updatedIntentions1);
                
                int updatedIntentions2 = jdbcTemplate.update("UPDATE users SET intention = 'CREATE_GROUPS' WHERE role IN ('ADMIN', 'SUPER_ADMIN') AND intention = 'UNDECIDED'");
                log.info("Updated {} users with ADMIN/SUPER_ADMIN role to CREATE_GROUPS intention", updatedIntentions2);
                
                // NOW add new role check constraint with updated values (after data is updated)
                try {
                    jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('MEMBER', 'ADMIN', 'SUPER_ADMIN'))");
                    log.info("Added new role check constraint");
                } catch (Exception e) {
                    log.warn("Could not add role check constraint: {}", e.getMessage());
                }
                
                // Add NOT NULL constraint after setting default values
                try {
                    jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN intention SET NOT NULL");
                    log.info("Set intention column to NOT NULL");
                } catch (Exception e) {
                    log.warn("Could not set intention column to NOT NULL: {}", e.getMessage());
                }
                
                log.info("Database migration completed successfully");
                
            } catch (Exception e) {
                log.error("Error during database migration: {}", e.getMessage());
                // Don't throw the exception - let the application start even if migration fails
                log.warn("Application will start without completing migration. Manual intervention may be required.");
            }
        };
    }
} 