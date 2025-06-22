package com.mahiberawi.controller;

import com.mahiberawi.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ApiResponse health() {
        return ApiResponse.builder()
                .success(true)
                .message("Mahiberawi Backend is running")
                .data("OK")
                .build();
    }
} 