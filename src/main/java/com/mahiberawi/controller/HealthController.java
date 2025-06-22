package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public ApiResponse root() {
        return ApiResponse.builder()
                .success(true)
                .message("Mahiberawi Backend API")
                .data("Server is running")
                .build();
    }
    
    @GetMapping("/health")
    public ApiResponse health() {
        return ApiResponse.builder()
                .success(true)
                .message("Mahiberawi Backend is running")
                .data("OK")
                .build();
    }
    
    @GetMapping("/api/health")
    public ApiResponse apiHealth() {
        return ApiResponse.builder()
                .success(true)
                .message("Mahiberawi Backend API is healthy")
                .data("OK")
                .build();
    }
} 