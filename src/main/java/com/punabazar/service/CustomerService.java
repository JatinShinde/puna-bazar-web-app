package com.punabazar.service;

import com.punabazar.dto.CustomerRequestDTO;
import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.Customer;
import com.punabazar.model.Transaction;
import com.punabazar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionService transactionService;

    public CustomerService(CustomerRepository customerRepository,
                           TransactionRepository transactionRepository,
                           PaymentRepository paymentRepository,
                           CommissionRepository commissionRepository,
                           LedgerRepository ledgerRepository,
                           TransactionService transactionService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.ledgerRepository = ledgerRepository;
        this.transactionService = transactionService;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = customerRepository.findAll();
        populateTodayNet(list);
        return list;
    }

    public Customer getCustomerById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        populateTodayNet(java.util.Collections.singletonList(c));
        return c;
    }

    public List<Customer> searchCustomers(String query) {
        List<Customer> list;
        if (query == null || query.trim().isEmpty()) {
            list = customerRepository.findAll();
        } else {
            list = customerRepository.searchCustomers(query.trim());
        }
        populateTodayNet(list);
        return list;
    }

    public List<Customer> getCustomersByCity(String city) {
        List<Customer> list = customerRepository.findByCityIgnoreCase(city);
        populateTodayNet(list);
        return list;
    }

    private void populateTodayNet(List<Customer> list) {
        if (list == null || list.isEmpty()) return;
        try {
            LocalDate today = LocalDate.now();
            List<Transaction> todayTxs = transactionRepository.findByTransactionDate(today);
            java.util.Map<Long, BigDecimal> todayNetMap = new java.util.HashMap<>();
            if (todayTxs != null) {
                for (Transaction tx : todayTxs) {
                    if (tx != null && tx.getCustomer() != null && tx.getCustomer().getId() != null) {
                        Long cId = tx.getCustomer().getId();
                        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
                        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
                        BigDecimal payPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
                        BigDecimal payPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;
                        BigDecimal netTx = (sellPo.add(sellPc)).subtract(payPo.add(payPc));
                        todayNetMap.put(cId, todayNetMap.getOrDefault(cId, BigDecimal.ZERO).add(netTx));
                    }
                }
            }
            for (Customer c : list) {
                if (c != null && c.getId() != null) {
                    c.setTodayNet(todayNetMap.getOrDefault(c.getId(), BigDecimal.ZERO));
                }
            }
        } catch (Exception e) {
            // Prevent any error from breaking customer list retrieval
        }
    }

    public List<String> getDistinctMarkets() {
        return customerRepository.findDistinctCities();
    }

    public Customer saveCustomer(Customer customer) {
        if (customer.getPreviousBalance() == null) {
            customer.setPreviousBalance(BigDecimal.ZERO);
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer createCustomerWithTrade(CustomerRequestDTO request) {
        BigDecimal yene = request.getYene() != null ? request.getYene() : BigDecimal.ZERO;
        BigDecimal dene = request.getDene() != null ? request.getDene() : BigDecimal.ZERO;
        BigDecimal effectiveMagilBaki = yene.subtract(dene);
        String balanceType = effectiveMagilBaki.compareTo(BigDecimal.ZERO) < 0 ? "DENE" : "YENE";
        BigDecimal magilBaki = effectiveMagilBaki.abs();

        String mobile = request.getMobileNumber() != null ? request.getMobileNumber() : "";
        String city = request.getCity() != null ? request.getCity() : "";
        String marketZone = request.getMarketZone() != null ? request.getMarketZone() : city;

        Customer customer = new Customer(
                request.getName(),
                mobile,
                city,
                marketZone,
                effectiveMagilBaki
        );

        customer.setYene(yene);
        customer.setDene(dene);
        if (request.getCommissionPercentage() != null) {
            customer.setCommissionRate(request.getCommissionPercentage());
        }
        if (request.getCommissionEnabled() != null) {
            customer.setCommissionEnabled(request.getCommissionEnabled());
        }
        if (request.getPagar() != null) {
            customer.setPagar(request.getPagar());
        }
        if (request.getPagarEnabled() != null) {
            customer.setPagarEnabled(request.getPagarEnabled());
        }
        customer.setMagilBaki(magilBaki);
        customer.setBalanceType(balanceType);
        if (request.getMarketCodes() != null && !request.getMarketCodes().trim().isEmpty()) {
            customer.setMarketCodes(request.getMarketCodes().trim());
        }
        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        if (request.getShareRate() != null) {
            customer.setShareRate(request.getShareRate());
        }
        if (request.getShare30ProfitOnly() != null) {
            customer.setShare30ProfitOnly(request.getShare30ProfitOnly());
        }

        customer = customerRepository.save(customer);

        BigDecimal sell = request.getSell() != null ? request.getSell() : BigDecimal.ZERO;
        BigDecimal payment = request.getPayment() != null ? request.getPayment() : BigDecimal.ZERO;
        Integer pc = request.getPc() != null ? request.getPc() : 0;
        BigDecimal commPercent = customer.getCommissionRate();

        if (sell.compareTo(BigDecimal.ZERO) > 0 || payment.compareTo(BigDecimal.ZERO) > 0 || pc > 0) {
            TransactionRequestDTO txReq = new TransactionRequestDTO();
            txReq.setCustomerId(customer.getId());
            txReq.setTransactionDate(LocalDate.now());
            txReq.setSellPo(sell);
            txReq.setSellPc(new BigDecimal(pc));
            txReq.setRate(BigDecimal.ONE);
            txReq.setCommissionPercentage(commPercent);
            txReq.setPaymentAmount(payment);
            txReq.setPaymentMode("Cash/UPI");
            txReq.setNotes("Opening Trade Entry");

            transactionService.processTransaction(txReq);
            customer = customerRepository.findById(customer.getId()).orElse(customer);
        }

        return customer;
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerRequestDTO request) {
        Customer customer = getCustomerById(id);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            customer.setName(request.getName().trim());
        }
        if (request.getCommissionPercentage() != null) {
            customer.setCommissionRate(request.getCommissionPercentage());
        }
        if (request.getCommissionEnabled() != null) {
            customer.setCommissionEnabled(request.getCommissionEnabled());
        }
        if (request.getPagar() != null) {
            customer.setPagar(request.getPagar());
        }
        if (request.getPagarEnabled() != null) {
            customer.setPagarEnabled(request.getPagarEnabled());
        }
        if (request.getShareRate() != null) {
            customer.setShareRate(request.getShareRate());
        }
        if (request.getShare30ProfitOnly() != null) {
            customer.setShare30ProfitOnly(request.getShare30ProfitOnly());
        }
        if (request.getMarketCodes() != null && !request.getMarketCodes().trim().isEmpty()) {
            customer.setMarketCodes(request.getMarketCodes().trim());
        }
        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (id == null) return;
        customerRepository.findById(id).ifPresent(customer -> {
            commissionRepository.deleteByCustomerId(id);
            transactionRepository.deleteByCustomerId(id);
            paymentRepository.deleteByCustomerId(id);
            ledgerRepository.deleteByCustomerId(id);
            customerRepository.delete(customer);
        });
    }
}
