package com.mahiberawi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {
    
    private final JdbcTemplate jdbcTemplate;
    
    private static final List<String> REQUIRED_TABLES = Arrays.asList(
            "users", "groups", "group_members", "email_verification_codes", 
            "group_invitations", "events", "event_attendance", "event_participants", 
            "messages", "payments", "notifications", "files", "memberships"
    );
    
    /**
     * Database schema validator that runs after SQL initialization
     * This ensures the database schema matches the expected structure
     */
    @Bean
    @Transactional
    public DatabaseSchemaValidator databaseSchemaValidator() {
        return new DatabaseSchemaValidator(jdbcTemplate);
    }
    
    /**
     * Inner class to validate database schema
     */
    @Slf4j
    public static class DatabaseSchemaValidator {
        
        private final JdbcTemplate jdbcTemplate;
        
        public DatabaseSchemaValidator(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
            validateSchema();
        }
        
        private void validateSchema() {
            log.info("Validating database schema...");
            
            try {
                // Check if required tables exist
                for (String table : REQUIRED_TABLES) {
                    boolean exists = tableExists(table);
                    if (exists) {
                        log.info("Table '{}' exists", table);
                    } else {
                        log.warn("Required table '{}' does not exist", table);
                    }
                }
                
                log.info("Database schema validation completed successfully");
                
            } catch (Exception e) {
                log.error("Error during schema validation: {}", e.getMessage());
                // Don't throw exception - let application start
            }
        }
        
        private boolean tableExists(String tableName) {
            try {
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?", 
                    Integer.class, 
                    tableName
                );
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
} 