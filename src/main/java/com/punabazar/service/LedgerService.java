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
        return getDashboardMetricsForDate(LocalDate.now());
    }

    public DashboardMetricsDTO getDashboardMetricsForDate(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<Customer> customers = customerRepository.findAll();
        List<com.punabazar.model.Transaction> todayTxs = transactionRepository.findByTransactionDate(targetDate);

        Double sellSum = transactionRepository.getTodayTotalSell(targetDate);
        Double poSum = transactionRepository.getTodayPoSell(targetDate);
        Double pcSum = transactionRepository.getTodayPcSell(targetDate);
        Double paySum = transactionRepository.getTodayTotalPayments(targetDate);
        Double commSum = commissionRepository.getTodayTotalCommission(targetDate);

        BigDecimal todayTotalSell = sellSum != null ? BigDecimal.valueOf(sellSum) : BigDecimal.ZERO;
        BigDecimal todayPoSell = poSum != null ? BigDecimal.valueOf(poSum) : BigDecimal.ZERO;
        BigDecimal todayPcSell = pcSum != null ? BigDecimal.valueOf(pcSum) : BigDecimal.ZERO;
        BigDecimal todayTotalPayment = paySum != null ? BigDecimal.valueOf(paySum) : BigDecimal.ZERO;
        BigDecimal todayTotalCommission = commSum != null ? BigDecimal.valueOf(commSum) : BigDecimal.ZERO;

        BigDecimal todayNetSum = BigDecimal.ZERO;
        BigDecimal todayTotalMissPayment = BigDecimal.ZERO;
        BigDecimal todayTotalPagar = BigDecimal.ZERO;
        BigDecimal todayTotal30Share = BigDecimal.ZERO;

        if (todayTxs != null && !todayTxs.isEmpty()) {
            for (com.punabazar.model.Transaction tx : todayTxs) {
                Customer customer = tx.getCustomer();
                if (tx.getFarak() != null) {
                    todayTotalMissPayment = todayTotalMissPayment.add(tx.getFarak());
                }
                if (tx.getPagarAmount() != null) {
                    todayTotalPagar = todayTotalPagar.add(tx.getPagarAmount());
                }
                BigDecimal shareAmt = WhatsAppService.calculateShareAmountForTransaction(customer, tx);
                todayTotal30Share = todayTotal30Share.add(shareAmt);

                BigDecimal txTodayNet = WhatsAppService.calculateReceiptTodayNet(customer, tx);
                todayNetSum = todayNetSum.add(txTodayNet);
            }
        }

        String profitLossStatus = todayNetSum.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS";
        BigDecimal todayProfitLoss = todayNetSum.abs();

        BigDecimal totalCustomerBalance = (customers != null) ? customers.stream()
                .map(c -> c.getPreviousBalance() != null ? c.getPreviousBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        List<String> markets = customerRepository.findDistinctCities();
        Long totalCustomerCount = (customers != null) ? (long) customers.size() : 0L;

        java.util.Set<Long> todayCustomerSet = new java.util.HashSet<>();
        java.util.Set<String> receiptMarketSet = new java.util.HashSet<>();
        if (todayTxs != null) {
            for (com.punabazar.model.Transaction tx : todayTxs) {
                if (tx.getCustomer() != null && tx.getCustomer().getId() != null) {
                    todayCustomerSet.add(tx.getCustomer().getId());
                    String mCity = tx.getCustomer().getCity() != null && !tx.getCustomer().getCity().trim().isEmpty()
                            ? tx.getCustomer().getCity().trim()
                            : (tx.getCustomer().getMarketZone() != null ? tx.getCustomer().getMarketZone().trim() : "General");
                    receiptMarketSet.add(mCity);
                }
            }
        }
        Long generatedReceiptCount = (long) todayCustomerSet.size();
        List<String> generatedReceiptMarkets = new java.util.ArrayList<>(receiptMarketSet);
        java.util.Collections.sort(generatedReceiptMarkets);

        DashboardMetricsDTO dto = new DashboardMetricsDTO(
                todayTotalSell,
                todayPoSell,
                todayPcSell,
                todayTotalPayment,
                todayTotalCommission,
                todayProfitLoss,
                profitLossStatus,
                totalCustomerBalance,
                totalCustomerCount,
                markets,
                generatedReceiptCount,
                totalCustomerCount,
                generatedReceiptMarkets
        );
        dto.setTodayTotalMissPayment(todayTotalMissPayment);
        dto.setTodayTotalPagar(todayTotalPagar);
        dto.setTodayTotal30Share(todayTotal30Share);
        return dto;
    }

    public List<Ledger> getCustomerLedger(Long customerId) {
        return ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customerId);
    }

    public java.util.Map<String, DashboardMetricsDTO> getWeeklyDailyProfitLoss(LocalDate date) {
        LocalDate refDate = date != null ? date : LocalDate.now();
        LocalDate startOfWeek = refDate.with(java.time.DayOfWeek.MONDAY);

        java.util.Map<String, DashboardMetricsDTO> map = new java.util.LinkedHashMap<>();
        for (java.time.DayOfWeek dow : java.time.DayOfWeek.values()) {
            LocalDate dayDate = startOfWeek.with(dow);
            map.put(dow.name(), getDashboardMetricsForDate(dayDate));
        }
        return map;
    }
}
