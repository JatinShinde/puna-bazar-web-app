package com.punabazar.controller;

import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.Ledger;
import com.punabazar.model.Transaction;
import com.punabazar.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public Ledger createTransaction(@RequestBody TransactionRequestDTO request) {
        return transactionService.processTransaction(request);
    }

    @PutMapping("/update")
    public Ledger updateTransaction(@RequestBody TransactionRequestDTO request) {
        return transactionService.updateTransaction(request);
    }

    @GetMapping("/check-exists")
    public Map<String, Boolean> checkExists(
            @RequestParam Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean exists = transactionService.checkTransactionExists(customerId, date != null ? date : LocalDate.now());
        return Collections.singletonMap("exists", exists);
    }

    @GetMapping("/customer/{customerId}")
    public List<Transaction> getCustomerTransactions(@PathVariable Long customerId) {
        return transactionService.getTransactionsByCustomer(customerId);
    }
}
