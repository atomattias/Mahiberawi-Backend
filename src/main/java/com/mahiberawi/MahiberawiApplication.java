package com.mahiberawi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MahiberawiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MahiberawiApplication.class, args);
    }
} 