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

        // 2. Seed Live Customers & Transactions from JSON backup if database is empty
        if (customerRepository.count() == 0) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("seed_customers.json");
                if (resource.exists()) {
                    try (java.io.InputStream inputStream = resource.getInputStream()) {
                        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(inputStream);
                        if (rootNode.isArray()) {
                            for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                                Customer c = new Customer();
                                c.setName(node.has("name") ? node.get("name").asText("") : "");
                                c.setMobileNumber(node.has("mobileNumber") ? node.get("mobileNumber").asText("") : "");
                                c.setCity(node.has("city") ? node.get("city").asText("") : "");
                                c.setMarketZone(node.has("marketZone") ? node.get("marketZone").asText("") : "");
                                c.setPreviousBalance(node.has("previousBalance") ? new BigDecimal(node.get("previousBalance").asText("0")) : BigDecimal.ZERO);
                                c.setCommissionRate(node.has("commissionRate") ? new BigDecimal(node.get("commissionRate").asText("10")) : new BigDecimal("10.00"));
                                c.setCommissionEnabled(node.has("commissionEnabled") && node.get("commissionEnabled").asBoolean(false));
                                c.setPagar(node.has("pagar") ? new BigDecimal(node.get("pagar").asText("0")) : BigDecimal.ZERO);
                                c.setPagarEnabled(node.has("pagarEnabled") && node.get("pagarEnabled").asBoolean(false));
                                c.setMagilBaki(node.has("magilBaki") ? new BigDecimal(node.get("magilBaki").asText("0")) : BigDecimal.ZERO);
                                c.setYene(node.has("yene") ? new BigDecimal(node.get("yene").asText("0")) : BigDecimal.ZERO);
                                c.setDene(node.has("dene") ? new BigDecimal(node.get("dene").asText("0")) : BigDecimal.ZERO);
                                c.setBalanceType(node.has("balanceType") ? node.get("balanceType").asText("YENE") : "YENE");
                                c.setFarak(node.has("farak") ? new BigDecimal(node.get("farak").asText("0")) : BigDecimal.ZERO);
                                c.setMarketCodes(node.has("marketCodes") ? node.get("marketCodes").asText("PO,PC") : "PO,PC");
                                c.setReceiptStyle(node.has("receiptStyle") ? node.get("receiptStyle").asText("TYPE_1") : "TYPE_1");
                                c.setShareRate(node.has("shareRate") ? new BigDecimal(node.get("shareRate").asText("100")) : new BigDecimal("100.00"));
                                c.setShare30ProfitOnly(node.has("share30ProfitOnly") && node.get("share30ProfitOnly").asBoolean(false));
                                customerRepository.save(c);
                            }
                            System.out.println(">>> Successfully seeded " + rootNode.size() + " live customers!");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> Error seeding customers from JSON backup: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 3. Seed & Sync Live Daily Transactions from seed_transactions.json
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            org.springframework.core.io.ClassPathResource txResource = new org.springframework.core.io.ClassPathResource("seed_transactions.json");
            if (txResource.exists()) {
                java.util.Map<String, Customer> nameToCustomer = new java.util.HashMap<>();
                for (Customer c : customerRepository.findAll()) {
                    if (c.getName() != null) {
                        nameToCustomer.put(c.getName().trim(), c);
                    }
                }

                try (java.io.InputStream inputStream = txResource.getInputStream()) {
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(inputStream);
                    if (rootNode.isArray()) {
                        int count = 0;
                        for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                            String custName = "";
                            if (node.has("customer") && node.get("customer").has("name")) {
                                custName = node.get("customer").get("name").asText("").trim();
                            }
                            Customer matched = nameToCustomer.get(custName);
                            if (matched != null) {
                                String txDateStr = node.has("transactionDate") ? node.get("transactionDate").asText("") : "";
                                LocalDate txDate = LocalDate.now();
                                if (!txDateStr.isEmpty()) {
                                    try { txDate = LocalDate.parse(txDateStr); } catch (Exception ignored) {}
                                }

                                BigDecimal sellPo = node.has("sellPo") ? new BigDecimal(node.get("sellPo").asText("0")) : BigDecimal.ZERO;
                                BigDecimal sellPc = node.has("sellPcAmount") ? new BigDecimal(node.get("sellPcAmount").asText("0")) : BigDecimal.ZERO;
                                BigDecimal payPo = node.has("paymentPo") ? new BigDecimal(node.get("paymentPo").asText("0")) : BigDecimal.ZERO;
                                BigDecimal payPc = node.has("paymentPc") ? new BigDecimal(node.get("paymentPc").asText("0")) : BigDecimal.ZERO;

                                java.util.List<Transaction> existingList = transactionRepository.findByCustomerIdAndTransactionDate(matched.getId(), txDate);
                                if (existingList != null && !existingList.isEmpty()) {
                                    Transaction current = existingList.get(0);
                                    current.setSellPo(sellPo);
                                    current.setSellPcAmount(sellPc);
                                    current.setPaymentPo(payPo);
                                    current.setPaymentPc(payPc);
                                    current.setTotalSell(sellPo.add(sellPc));
                                    transactionRepository.save(current);
                                } else {
                                    Transaction stx = new Transaction();
                                    stx.setCustomer(matched);
                                    stx.setTransactionDate(txDate);
                                    stx.setSellPo(sellPo);
                                    stx.setSellPcAmount(sellPc);
                                    stx.setPaymentPo(payPo);
                                    stx.setPaymentPc(payPc);
                                    stx.setTotalSell(sellPo.add(sellPc));
                                    transactionRepository.save(stx);
                                }
                                count++;
                            }
                        }
                        System.out.println(">>> Successfully synced " + count + " live daily transactions!");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(">>> Error syncing transactions from JSON backup: " + e.getMessage());
            e.printStackTrace();
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
