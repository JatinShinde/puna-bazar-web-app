package com.punabazar.service;

import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.*;
import com.punabazar.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public boolean checkTransactionExists(Long customerId, LocalDate date) {
        LocalDate checkDate = date != null ? date : LocalDate.now();
        return transactionRepository.existsByCustomerIdAndTransactionDate(customerId, checkDate);
    }

    @Transactional
    public Ledger processTransaction(TransactionRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        LocalDate date = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now();

        // 1. Enforce: One market entry per customer per date only via Daily Trade Entry
        if (transactionRepository.existsByCustomerIdAndTransactionDate(request.getCustomerId(), date)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "⚠️ Market entry for " + customer.getName() + " on " + date + " is already uploaded! Changes are not accepted from Daily Trade Entry & Math Engine. Please use 'Edit Full Receipt' to make changes."
            );
        }

        BigDecimal sellPo = request.getSellPo() != null ? request.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = request.getSellPc() != null ? request.getSellPc() : BigDecimal.ZERO;
        BigDecimal payPo = request.getPaymentPo() != null ? request.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = request.getPaymentPc() != null ? request.getPaymentPc() : BigDecimal.ZERO;
        BigDecimal magilBaki = request.getMagilBaki() != null ? request.getMagilBaki() : (customer.getPreviousBalance() != null ? customer.getPreviousBalance() : BigDecimal.ZERO);
        BigDecimal pagarAmount = request.getPagarAmount() != null ? request.getPagarAmount() : BigDecimal.ZERO;

        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        BigDecimal farakVal = request.getFarak() != null ? request.getFarak() : BigDecimal.ZERO;

        // 2. Save Transaction
        Transaction tx = new Transaction(customer, date, sellPo, sellPc, payPo, payPc, magilBaki, pagarAmount, totalSell);
        tx.setFarak(farakVal);
        tx = transactionRepository.save(tx);

        // 3. Compute and Save Commission (ONLY if selected/enabled for customer)
        boolean isCommEnabled = Boolean.TRUE.equals(customer.getCommissionEnabled());
        BigDecimal commPercent = (isCommEnabled && request.getCommissionPercentage() != null) ? request.getCommissionPercentage() : new BigDecimal("10.00");
        BigDecimal commissionAmount = isCommEnabled ? calculationEngineService.calculateCommission(totalSell, commPercent) : BigDecimal.ZERO;
        Commission commission = new Commission(tx, commPercent, commissionAmount);
        commissionRepository.save(commission);

        // 4. Save Payment if provided
        if (totalPayment.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment(customer, date, totalPayment, request.getPaymentMode() != null ? request.getPaymentMode() : "Cash", request.getNotes());
            paymentRepository.save(payment);
        }

        // 5. Compute Net Balance Due with Share deduction included (e.g. 40/60 share, 30% profit share)
        BigDecimal shareRateVal = request.getShareRate() != null ? request.getShareRate() : (customer.getShareRate() != null ? customer.getShareRate() : new BigDecimal("100.00"));
        Boolean share30Val = customer.getShare30ProfitOnly();

        BigDecimal netBalance = calculationEngineService.calculateNetBalanceDueWithShare(
                magilBaki, totalSell, commissionAmount, totalPayment, pagarAmount, farakVal, shareRateVal, share30Val
        );

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
        customer.setPreviousBalance(netBalance);
        customerRepository.save(customer);

        return ledger;
    }

    @Transactional
    public Ledger updateTransaction(TransactionRequestDTO request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        LocalDate date = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now();

        List<Transaction> existingTxs = transactionRepository.findByCustomerIdAndTransactionDate(request.getCustomerId(), date);
        Transaction tx;
        if (!existingTxs.isEmpty()) {
            tx = existingTxs.get(0);
        } else {
            List<Transaction> allTxs = transactionRepository.findByCustomerIdOrderByTransactionDateDesc(request.getCustomerId());
            if (!allTxs.isEmpty()) {
                tx = allTxs.get(0);
            } else {
                tx = new Transaction();
                tx.setCustomer(customer);
                tx.setTransactionDate(date);
            }
        }

        BigDecimal sellPo = request.getSellPo() != null ? request.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = request.getSellPc() != null ? request.getSellPc() : BigDecimal.ZERO;
        BigDecimal payPo = request.getPaymentPo() != null ? request.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = request.getPaymentPc() != null ? request.getPaymentPc() : BigDecimal.ZERO;
        BigDecimal magilBaki = request.getMagilBaki() != null ? request.getMagilBaki() : (customer.getPreviousBalance() != null ? customer.getPreviousBalance() : BigDecimal.ZERO);
        BigDecimal pagarAmount = request.getPagarAmount() != null ? request.getPagarAmount() : BigDecimal.ZERO;

        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        tx.setSellPo(sellPo);
        tx.setSellPcAmount(sellPc);
        tx.setPaymentPo(payPo);
        tx.setPaymentPc(payPc);
        tx.setMagilBaki(magilBaki);
        tx.setPagarAmount(pagarAmount);
        tx.setTotalSell(totalSell);
        tx = transactionRepository.save(tx);

        boolean isCommEnabled = Boolean.TRUE.equals(customer.getCommissionEnabled());
        BigDecimal commPercent = (isCommEnabled && request.getCommissionPercentage() != null) ? request.getCommissionPercentage() : (customer.getCommissionRate() != null ? customer.getCommissionRate() : new BigDecimal("10.00"));
        BigDecimal commissionAmount = isCommEnabled ? calculationEngineService.calculateCommission(totalSell, commPercent) : BigDecimal.ZERO;

        List<Ledger> customerLedgers = ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customer.getId());
        Ledger ledger;
        if (!customerLedgers.isEmpty()) {
            ledger = customerLedgers.get(0);
            ledger.setTotalSell(totalSell);
            ledger.setTotalCommission(commissionAmount);
            ledger.setTotalPayment(totalPayment);
            ledger.setPreviousBalance(magilBaki);
        } else {
            ledger = new Ledger();
            ledger.setCustomer(customer);
            ledger.setEntryDate(date);
            ledger.setTotalSell(totalSell);
            ledger.setTotalCommission(commissionAmount);
            ledger.setTotalPayment(totalPayment);
            ledger.setPreviousBalance(magilBaki);
        }

        BigDecimal farakVal = request.getFarak() != null ? request.getFarak() : (tx.getFarak() != null ? tx.getFarak() : BigDecimal.ZERO);
        tx.setFarak(farakVal);
        transactionRepository.save(tx);

        if (magilBaki.compareTo(BigDecimal.ZERO) > 0) {
            customer.setYene(magilBaki);
            customer.setDene(BigDecimal.ZERO);
            customer.setMagilBaki(magilBaki);
        } else if (magilBaki.compareTo(BigDecimal.ZERO) < 0) {
            customer.setDene(magilBaki.abs());
            customer.setYene(BigDecimal.ZERO);
            customer.setMagilBaki(magilBaki.abs());
        }
        customer.setFarak(farakVal);

        BigDecimal shareRateVal = request.getShareRate() != null ? request.getShareRate() : (customer.getShareRate() != null ? customer.getShareRate() : new BigDecimal("100.00"));
        Boolean share30Val = customer.getShare30ProfitOnly();

        BigDecimal netBalance = calculationEngineService.calculateNetBalanceDueWithShare(
                magilBaki, totalSell, commissionAmount, totalPayment, pagarAmount, farakVal, shareRateVal, share30Val
        );
        ledger.setNetBalanceDue(netBalance);
        ledger = ledgerRepository.save(ledger);

        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        if (request.getShareRate() != null) {
            customer.setShareRate(request.getShareRate());
        }
        customer.setPreviousBalance(netBalance);
        customerRepository.save(customer);

        return ledger;
    }

    public List<Transaction> getTransactionsByCustomer(Long customerId) {
        return transactionRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
    }
}
