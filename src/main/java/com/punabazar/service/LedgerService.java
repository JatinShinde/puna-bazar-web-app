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

        if (todayTxs != null && !todayTxs.isEmpty()) {
            for (com.punabazar.model.Transaction tx : todayTxs) {
                Customer customer = tx.getCustomer();
                BigDecimal totalSell = tx.getTotalSell() != null ? tx.getTotalSell() : BigDecimal.ZERO;
                BigDecimal paymentPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
                BigDecimal paymentPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;
                BigDecimal totalPay = paymentPo.add(paymentPc);

                boolean isCommEnabled = customer != null && Boolean.TRUE.equals(customer.getCommissionEnabled());
                BigDecimal commRate = (customer != null && customer.getCommissionRate() != null) ? customer.getCommissionRate() : new BigDecimal("10.00");
                BigDecimal commCut = isCommEnabled ? totalSell.multiply(commRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

                BigDecimal totalAfterComm = totalSell.subtract(commCut);
                BigDecimal afterPay = totalAfterComm.subtract(totalPay);

                BigDecimal pagarVal = tx.getPagarAmount() != null ? tx.getPagarAmount() : (customer != null && customer.getPagar() != null ? customer.getPagar() : BigDecimal.ZERO);
                BigDecimal farakVal = customer != null && customer.getFarak() != null ? customer.getFarak() : BigDecimal.ZERO;

                BigDecimal runningNet = afterPay.subtract(pagarVal).subtract(farakVal);

                boolean is30ProfitOnly = customer != null && Boolean.TRUE.equals(customer.getShare30ProfitOnly());
                BigDecimal shareRate = (customer != null && customer.getShareRate() != null) ? customer.getShareRate() : new BigDecimal("100.00");
                BigDecimal rateToApply = is30ProfitOnly ? new BigDecimal("30.00") : shareRate;
                boolean isShareEnabled = is30ProfitOnly || (shareRate.compareTo(new BigDecimal("100.00")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0);

                BigDecimal shareAmount = BigDecimal.ZERO;
                if (isShareEnabled) {
                    if (is30ProfitOnly) {
                        if (runningNet.compareTo(BigDecimal.ZERO) > 0) {
                            shareAmount = runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        }
                    } else {
                        shareAmount = runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    }
                }

                BigDecimal txTodayNet = runningNet.subtract(shareAmount);
                todayNetSum = todayNetSum.add(txTodayNet);
            }
        }

        String profitLossStatus = todayNetSum.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS";
        BigDecimal todayProfitLoss = todayNetSum.abs();

        BigDecimal totalCustomerBalance = (customers != null) ? customers.stream()
                .map(c -> c.getPreviousBalance() != null ? c.getPreviousBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        List<String> markets = customerRepository.findDistinctCities();
        List<com.punabazar.model.Transaction> allTxs = transactionRepository.findAll();
        List<Ledger> allLedgers = ledgerRepository.findAll();
        java.util.Set<Long> activeCustomerIdsWithReceipts = new java.util.HashSet<>();

        if (allTxs != null) {
            for (com.punabazar.model.Transaction tx : allTxs) {
                if (tx.getCustomer() != null && tx.getCustomer().getId() != null) {
                    activeCustomerIdsWithReceipts.add(tx.getCustomer().getId());
                }
            }
        }

        if (allLedgers != null) {
            for (Ledger l : allLedgers) {
                if (l.getCustomer() != null && l.getCustomer().getId() != null) {
                    activeCustomerIdsWithReceipts.add(l.getCustomer().getId());
                }
            }
        }

        if (customers != null) {
            for (Customer c : customers) {
                if (c.getId() != null) {
                    boolean hasBalance = (c.getPreviousBalance() != null && c.getPreviousBalance().compareTo(BigDecimal.ZERO) != 0)
                            || (c.getYene() != null && c.getYene().compareTo(BigDecimal.ZERO) != 0)
                            || (c.getDene() != null && c.getDene().compareTo(BigDecimal.ZERO) != 0)
                            || (c.getMagilBaki() != null && c.getMagilBaki().compareTo(BigDecimal.ZERO) != 0);
                    if (hasBalance) {
                        activeCustomerIdsWithReceipts.add(c.getId());
                    }
                }
            }
        }

        long generatedReceiptCount = activeCustomerIdsWithReceipts.size();
        long totalCustomerCount = customers != null ? customers.size() : 0;

        List<String> generatedReceiptMarkets = new java.util.ArrayList<>();
        if (customers != null) {
            for (Customer c : customers) {
                if (c.getId() != null && activeCustomerIdsWithReceipts.contains(c.getId())) {
                    String cityStr = c.getCity() != null ? c.getCity().trim() : "";
                    String nameStr = c.getName() != null ? c.getName().trim() : "";
                    String label = (!cityStr.isEmpty()) ? cityStr + " (" + nameStr + ")" : nameStr + " (General Market)";
                    if (!generatedReceiptMarkets.contains(label)) {
                        generatedReceiptMarkets.add(label);
                    }
                }
            }
        }

        return new DashboardMetricsDTO(
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
    }

    public List<Ledger> getCustomerLedger(Long customerId) {
        return ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customerId);
    }
}
