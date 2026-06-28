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

    @GetMapping("/performance")
    public ResponseEntity<PerformanceResponse.PerformanceResult> getUserPerformance() {
        return ResponseEntity.ok(performanceService.getPerformance());
    }
}
