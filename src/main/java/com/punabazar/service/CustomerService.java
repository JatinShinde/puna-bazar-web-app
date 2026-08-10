package com.punabazar.service;

import com.punabazar.dto.CustomerRequestDTO;
import com.punabazar.dto.TransactionRequestDTO;
import com.punabazar.model.Customer;
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
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return customerRepository.findAll();
        }
        return customerRepository.searchCustomers(query.trim());
    }

    public List<Customer> getCustomersByCity(String city) {
        return customerRepository.findByCityIgnoreCase(city);
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
        if (request.getFarak() != null) {
            customer.setFarak(request.getFarak());
        }
        if (request.getMarketCodes() != null && !request.getMarketCodes().trim().isEmpty()) {
            customer.setMarketCodes(request.getMarketCodes().trim());
        }
        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        if (request.getShareRate() != null) {
            customer.setShareRate(request.getShareRate());
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
        if (request.getReceiptStyle() != null && !request.getReceiptStyle().trim().isEmpty()) {
            customer.setReceiptStyle(request.getReceiptStyle().trim().toUpperCase());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        commissionRepository.deleteByCustomerId(id);
        transactionRepository.deleteByCustomerId(id);
        paymentRepository.deleteByCustomerId(id);
        ledgerRepository.deleteByCustomerId(id);
        customerRepository.delete(customer);
    }
}
