package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.model.PerformanceResponse;
import com.evaluate.ai.langchain.service.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/users/{userId}/performance")
    public ResponseEntity<PerformanceResponse.PerformanceResult> getUserPerformance(@PathVariable UUID userId) {
        return ResponseEntity.ok(performanceService.getPerformance(userId));
    }

    // Optional: convenience endpoint for current user when authentication is added later
    @GetMapping("/performance/me")
    public ResponseEntity<PerformanceResponse.PerformanceResult> getMyPerformance(@RequestParam UUID userId) {
        // For now, accept userId as param; replace with authenticated principal when security is configured
        return ResponseEntity.ok(performanceService.getPerformance(userId));
    }
}

