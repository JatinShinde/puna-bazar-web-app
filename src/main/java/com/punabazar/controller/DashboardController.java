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
    public DashboardMetricsDTO getMetrics(
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date) {
        return ledgerService.getDashboardMetricsForDate(date != null ? date : java.time.LocalDate.now());
    }

    @GetMapping("/weekly-daily-profit-loss")
    public java.util.Map<String, DashboardMetricsDTO> getWeeklyDailyProfitLoss(
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date) {
        return ledgerService.getWeeklyDailyProfitLoss(date);
    }
}
