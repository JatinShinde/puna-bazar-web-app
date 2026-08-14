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
        Double poSum = transactionRepository.getTodayPoSell(today);
        Double pcSum = transactionRepository.getTodayPcSell(today);
        Double paySum = paymentRepository.getTodayTotalPayments(today);
        Double commSum = commissionRepository.getTodayTotalCommission(today);

        BigDecimal todayTotalSell = sellSum != null ? BigDecimal.valueOf(sellSum) : BigDecimal.ZERO;
        BigDecimal todayPoSell = poSum != null ? BigDecimal.valueOf(poSum) : BigDecimal.ZERO;
        BigDecimal todayPcSell = pcSum != null ? BigDecimal.valueOf(pcSum) : BigDecimal.ZERO;
        BigDecimal todayTotalPayment = paySum != null ? BigDecimal.valueOf(paySum) : BigDecimal.ZERO;
        BigDecimal todayTotalCommission = commSum != null ? BigDecimal.valueOf(commSum) : BigDecimal.ZERO;

        List<com.punabazar.model.Transaction> todayTxs = transactionRepository.findByTransactionDate(today);
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

                BigDecimal shareRate = customer != null && customer.getShareRate() != null ? customer.getShareRate() : new BigDecimal("100.00");
                boolean isShareEnabled = shareRate.compareTo(new BigDecimal("100.00")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0;
                boolean is30ProfitOnly = customer != null && (Boolean.TRUE.equals(customer.getShare30ProfitOnly()) || shareRate.compareTo(new BigDecimal("30.00")) == 0);

                BigDecimal shareAmount = BigDecimal.ZERO;
                if (isShareEnabled) {
                    if (is30ProfitOnly) {
                        if (runningNet.compareTo(BigDecimal.ZERO) > 0) {
                            shareAmount = runningNet.multiply(shareRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        }
                    } else {
                        shareAmount = runningNet.multiply(shareRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    }
                }

                BigDecimal txTodayNet = runningNet.subtract(shareAmount);
                todayNetSum = todayNetSum.add(txTodayNet);
            }
        } else {
            todayNetSum = todayTotalSell.subtract(todayTotalCommission).subtract(todayTotalPayment);
        }

        String profitLossStatus = todayNetSum.compareTo(BigDecimal.ZERO) >= 0 ? "PROFIT" : "LOSS";
        BigDecimal todayProfitLoss = todayNetSum.abs();

        List<Customer> customers = customerRepository.findAll();
        BigDecimal totalCustomerBalance = customers.stream()
                .map(Customer::getPreviousBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> markets = customerRepository.findDistinctCities();

        return new DashboardMetricsDTO(
                todayTotalSell,
                todayPoSell,
                todayPcSell,
                todayTotalPayment,
                todayTotalCommission,
                todayProfitLoss,
                profitLossStatus,
                totalCustomerBalance,
                (long) customers.size(),
                markets
        );
    }

    public List<Ledger> getCustomerLedger(Long customerId) {
        return ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customerId);
    }
}
