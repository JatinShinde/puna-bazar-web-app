package com.punabazar.controller;

import com.punabazar.dto.WeeklyReceiptDTO;
import com.punabazar.service.WeeklyReceiptService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weekly-receipts")
public class WeeklyReceiptController {

    private final WeeklyReceiptService weeklyReceiptService;

    public WeeklyReceiptController(WeeklyReceiptService weeklyReceiptService) {
        this.weeklyReceiptService = weeklyReceiptService;
    }

    @GetMapping
    public List<WeeklyReceiptDTO> getWeeklyReceipts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return weeklyReceiptService.getWeeklyReceipts(date != null ? date : LocalDate.now());
    }
}
