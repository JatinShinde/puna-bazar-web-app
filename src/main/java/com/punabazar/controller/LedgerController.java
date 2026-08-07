package com.punabazar.controller;

import com.punabazar.model.Ledger;
import com.punabazar.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/customer/{customerId}")
    public List<Ledger> getLedgerForCustomer(@PathVariable Long customerId) {
        return ledgerService.getCustomerLedger(customerId);
    }
}
