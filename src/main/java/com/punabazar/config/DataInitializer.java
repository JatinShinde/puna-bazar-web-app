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

        // 2. Seed Customers across Markets if empty
        if (customerRepository.count() == 0) {
            Customer c1 = createCustomer("Ramesh Patil", "9822012345", "Solapur", "Solapur Market Yard", new BigDecimal("0.00"));
            Customer c2 = createCustomer("Swargate Traders", "9881023456", "Pune", "Gultekdi Market", new BigDecimal("0.00"));
            Customer c3 = createCustomer("Vashi APMC Trader", "9820045678", "Mumbai", "Vashi APMC", new BigDecimal("0.00"));
            Customer c4 = createCustomer("Mahalaxmi Trading Co", "9822367890", "Kolhapur", "Kolhapur Bazaar", new BigDecimal("0.00"));
            Customer c5 = createCustomer("Sangli Produce House", "9422478901", "Sangli", "Sangli Grain Market", new BigDecimal("0.00"));

            // Seed Sample 2-Column Transaction for Solapur (Ramesh Patil)
            seedTransaction(c1, LocalDate.now(), new BigDecimal("25000.00"), new BigDecimal("10000.00"), new BigDecimal("20000.00"), new BigDecimal("8000.00"), new BigDecimal("10.00"), "Solapur Market Trade");
            
            // Seed Sample Transaction for Pune (Swargate Traders)
            seedTransaction(c2, LocalDate.now(), new BigDecimal("60000.00"), new BigDecimal("15000.00"), new BigDecimal("30000.00"), new BigDecimal("10000.00"), new BigDecimal("10.00"), "Pune Market Entry");
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
