package com.punabazar.service;

import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.*;
import com.punabazar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final LedgerRepository ledgerRepository;
    private final CalculationEngineService calculationEngineService;

    public TransactionService(CustomerRepository customerRepository,
                              TransactionRepository transactionRepository,
                              PaymentRepository paymentRepository,
                              CommissionRepository commissionRepository,
                              LedgerRepository ledgerRepository,
                              CalculationEngineService calculationEngineService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.ledgerRepository = ledgerRepository;
        this.calculationEngineService = calculationEngineService;
    }

    @Transactional
    public Ledger processTransaction(TransactionRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        LocalDate date = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now();

        BigDecimal sellPo = request.getSellPo() != null ? request.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = request.getSellPc() != null ? request.getSellPc() : BigDecimal.ZERO;
        BigDecimal payPo = request.getPaymentPo() != null ? request.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = request.getPaymentPc() != null ? request.getPaymentPc() : BigDecimal.ZERO;
        BigDecimal magilBaki = request.getMagilBaki() != null ? request.getMagilBaki() : (customer.getPreviousBalance() != null ? customer.getPreviousBalance() : BigDecimal.ZERO);
        BigDecimal pagarAmount = request.getPagarAmount() != null ? request.getPagarAmount() : BigDecimal.ZERO;

        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        // 2. Save Transaction
        Transaction tx = new Transaction(customer, date, sellPo, sellPc, payPo, payPc, magilBaki, pagarAmount, totalSell);
        tx = transactionRepository.save(tx);

        // 3. Compute and Save Commission
        BigDecimal commPercent = request.getCommissionPercentage() != null ? request.getCommissionPercentage() : new BigDecimal("10.00");
        BigDecimal commissionAmount = calculationEngineService.calculateCommission(totalSell, commPercent);
        Commission commission = new Commission(tx, commPercent, commissionAmount);
        commissionRepository.save(commission);

        // 4. Save Payment if provided
        if (totalPayment.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment(customer, date, totalPayment, request.getPaymentMode() != null ? request.getPaymentMode() : "Cash", request.getNotes());
            paymentRepository.save(payment);
        }

        // 5. Compute Net Balance Due (Total yeṇe = magilBaki + totalSell - commissionAmount - totalPayment - pagarAmount)
        BigDecimal netBalance = magilBaki.add(totalSell).subtract(commissionAmount).subtract(totalPayment).subtract(pagarAmount);

        // 6. Create Ledger Entry
        Ledger ledger = new Ledger(customer, date, totalSell, commissionAmount, totalPayment, magilBaki, netBalance);
        ledger = ledgerRepository.save(ledger);

        // 7. Update Customer's running balance and receipt style settings
        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        if (request.getShareRate() != null) {
            customer.setShareRate(request.getShareRate());
        }
        if (request.getFarak() != null) {
            customer.setFarak(request.getFarak());
        }
        customer.setPreviousBalance(netBalance);
        customerRepository.save(customer);

        return ledger;
    }

    public List<Transaction> getTransactionsByCustomer(Long customerId) {
        return transactionRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
    }
}
