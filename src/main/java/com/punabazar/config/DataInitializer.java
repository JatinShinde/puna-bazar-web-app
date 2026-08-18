package com.punabazar.config;

import com.punabazar.model.*;
import com.punabazar.repository.*;
import com.punabazar.service.CalculationEngineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final LedgerRepository ledgerRepository;
    private final WhatsAppTemplateRepository whatsappTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final CalculationEngineService calculationEngineService;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           TransactionRepository transactionRepository,
                           PaymentRepository paymentRepository,
                           CommissionRepository commissionRepository,
                           LedgerRepository ledgerRepository,
                           WhatsAppTemplateRepository whatsappTemplateRepository,
                           PasswordEncoder passwordEncoder,
                           CalculationEngineService calculationEngineService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.ledgerRepository = ledgerRepository;
        this.whatsappTemplateRepository = whatsappTemplateRepository;
        this.passwordEncoder = passwordEncoder;
        this.calculationEngineService = calculationEngineService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@punabazar.com", "ROLE_ADMIN");
            userRepository.save(admin);
        }

        // 2. Seed Live Customers from JSON backup if database is empty
        if (customerRepository.count() == 0) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.findAndRegisterModules();
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("seed_customers.json");
                if (resource.exists()) {
                    try (java.io.InputStream inputStream = resource.getInputStream()) {
                        java.util.List<Customer> seedCustomers = mapper.readValue(
                            inputStream, 
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Customer>>() {}
                        );
                        for (Customer c : seedCustomers) {
                            c.setId(null);
                            customerRepository.save(c);
                        }
                        System.out.println(">>> Successfully seeded " + seedCustomers.size() + " live customers into MySQL!");
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> Error seeding customers from JSON backup: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Customer createCustomer(String name, String mobile, String city, String zone, BigDecimal prevBalance) {
        Customer c = new Customer(name, mobile, city, zone, prevBalance);
        return customerRepository.save(c);
    }

    private void seedTransaction(Customer customer, LocalDate date, BigDecimal sellPo, BigDecimal sellPc, BigDecimal payPo, BigDecimal payPc, BigDecimal commPercent, String payNotes) {
        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        Transaction tx = transactionRepository.save(new Transaction(customer, date, sellPo, sellPc, payPo, payPc, totalSell));
        
        BigDecimal commissionAmount = calculationEngineService.calculateCommission(totalSell, commPercent);
        commissionRepository.save(new Commission(tx, commPercent, commissionAmount));

        if (totalPayment.compareTo(BigDecimal.ZERO) > 0) {
            paymentRepository.save(new Payment(customer, date, totalPayment, "UPI/Cash", payNotes));
        }

        BigDecimal prevBal = customer.getPreviousBalance();
        BigDecimal netBal = prevBal.add(totalSell).subtract(commissionAmount).subtract(totalPayment);

        ledgerRepository.save(new Ledger(customer, date, totalSell, commissionAmount, totalPayment, prevBal, netBal));

        customer.setPreviousBalance(netBal);
        customerRepository.save(customer);
    }
}
