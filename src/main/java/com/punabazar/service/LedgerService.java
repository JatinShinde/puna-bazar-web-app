package com.punabazar.service;

import com.punabazar.dto.DashboardMetricsDTO;
import com.punabazar.model.Customer;
import com.punabazar.model.Ledger;
import com.punabazar.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LedgerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final LedgerRepository ledgerRepository;

    public LedgerService(CustomerRepository customerRepository,
                         TransactionRepository transactionRepository,
                         PaymentRepository paymentRepository,
                         CommissionRepository commissionRepository,
                         LedgerRepository ledgerRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public DashboardMetricsDTO getDashboardMetrics() {
        LocalDate today = LocalDate.now();

        Double sellSum = transactionRepository.getTodayTotalSell(today);
        Double paySum = paymentRepository.getTodayTotalPayments(today);
        Double commSum = commissionRepository.getTodayTotalCommission(today);

        BigDecimal todayTotalSell = sellSum != null ? BigDecimal.valueOf(sellSum) : BigDecimal.ZERO;
        BigDecimal todayTotalPayment = paySum != null ? BigDecimal.valueOf(paySum) : BigDecimal.ZERO;
        BigDecimal todayTotalCommission = commSum != null ? BigDecimal.valueOf(commSum) : BigDecimal.ZERO;

        List<Customer> customers = customerRepository.findAll();
        BigDecimal totalCustomerBalance = customers.stream()
                .map(Customer::getPreviousBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> markets = customerRepository.findDistinctCities();

        return new DashboardMetricsDTO(
                todayTotalSell,
                todayTotalPayment,
                todayTotalCommission,
                totalCustomerBalance,
                (long) customers.size(),
                markets
        );
    }

    public List<Ledger> getCustomerLedger(Long customerId) {
        return ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customerId);
    }
}
