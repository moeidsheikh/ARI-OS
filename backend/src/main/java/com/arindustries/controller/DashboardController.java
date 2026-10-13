package com.arindustries.controller;

import com.arindustries.dto.response.DashboardResponse;
import com.arindustries.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Plant Telemetry & Business Metric Aggregations")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/metrics"})
    @Operation(summary = "Get aggregated real-time plant and financial dashboard metrics")
    public ResponseEntity<DashboardResponse> getMetrics() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }
}
