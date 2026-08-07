package com.punabazar.controller;

import com.punabazar.dto.DashboardMetricsDTO;
import com.punabazar.service.LedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final LedgerService ledgerService;

    public DashboardController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/metrics")
    public DashboardMetricsDTO getMetrics() {
        return ledgerService.getDashboardMetrics();
    }
}
