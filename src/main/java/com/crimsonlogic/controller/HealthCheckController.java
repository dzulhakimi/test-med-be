package com.crimsonlogic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check endpoint for deployment health checks.
 * This endpoint bypasses authentication and returns immediately.
 */
@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "medical-cert-backend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Medical Certificate Verification API");
        response.put("status", "running");
        return ResponseEntity.ok(response);
    }
}
