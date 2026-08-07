package com.punabazar.controller;

import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.Ledger;
import com.punabazar.model.Transaction;
import com.punabazar.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/customer/{customerId}")
    public List<Transaction> getCustomerTransactions(@PathVariable Long customerId) {
        return transactionService.getTransactionsByCustomer(customerId);
    }
}
