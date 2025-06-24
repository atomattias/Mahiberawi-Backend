package com.mahiberawi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@Slf4j
public class MahiberawiApplication {
    public static void main(String[] args) {
        log.info("Starting Mahiberawi Backend Application...");
        log.info("Java version: {}", System.getProperty("java.version"));
        log.info("Spring profiles: {}", System.getProperty("spring.profiles.active", "default"));
        
        try {
            SpringApplication.run(MahiberawiApplication.class, args);
            log.info("Mahiberawi Backend Application started successfully!");
        } catch (Exception e) {
            log.error("Failed to start Mahiberawi Backend Application: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application is ready to serve requests!");
        log.info("Health check available at: /");
        log.info("API health check available at: /api/health");
    }
} 