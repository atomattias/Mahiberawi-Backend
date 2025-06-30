package com.mahiberawi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        // Convert Railway PostgreSQL URL to JDBC format
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            if (databaseUrl.startsWith("postgresql://")) {
                // Convert postgresql:// to jdbc:postgresql://
                String jdbcUrl = "jdbc:" + databaseUrl;
                dataSource.setJdbcUrl(jdbcUrl);
            } else {
                dataSource.setJdbcUrl(databaseUrl);
            }
        } else {
            // Fallback to H2 for development
            dataSource.setJdbcUrl("jdbc:h2:mem:testdb");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
        }
        
        return dataSource;
    }
} 