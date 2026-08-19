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
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@punabazar.com", "ROLE_ADMIN");
            userRepository.save(admin);
        }

        // 2. Seed Live Customers & Transactions from JSON backup if database is empty
        if (customerRepository.count() == 0) {
            try {
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("seed_customers.json");
                if (resource.exists()) {
                    try (java.io.InputStream inputStream = resource.getInputStream()) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(inputStream);
                        if (rootNode != null && rootNode.isArray() && rootNode.size() > 0) {
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
                            System.out.println(">>> Successfully seeded " + rootNode.size() + " live customers from JSON!");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> Error reading seed_customers.json: " + e.getMessage());
            }

            if (customerRepository.count() == 0) {
                seedDefaultLiveCustomers();
                System.out.println(">>> Successfully seeded 36 live market customers from fallback initializer!");
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
                    if (rootNode != null && rootNode.isArray()) {
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

    private void seedDefaultLiveCustomers() {
        Object[][] list = new Object[][] {
            {"साई 1", "", "", "", new BigDecimal("-10990.00"), new BigDecimal("300.00"), true, new BigDecimal("1600.00"), false, new BigDecimal("100.00"), false},
            {"साई 2 ", "", "", "", new BigDecimal("2420.00"), new BigDecimal("300.00"), true, new BigDecimal("0.00"), false, new BigDecimal("100.00"), false},
            {"काष्टी", "", "", "", new BigDecimal("-6360.00"), new BigDecimal("300.00"), false, new BigDecimal("0.00"), true, new BigDecimal("100.00"), false},
            {"खेडेकर ", "", "", "", new BigDecimal("4500.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"खेड ", "", "", "", new BigDecimal("3290.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, false, new BigDecimal("100.00"), false},
            {"सावंत ", "", "", "", new BigDecimal("1911.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"nb( बारामती )", "", "", "", new BigDecimal("32618.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"साखरवाडी ", "", "", "", new BigDecimal("-1599.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"भरणेनाका ", "", "", "", new BigDecimal("3648.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"जाणवलकर ", "", "", "", new BigDecimal("345.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, false, new BigDecimal("100.00"), false},
            {"पवार शेठ ", "", "", "", BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("40.00"), false},
            {"रॉकी शेठ ", "", "", "", new BigDecimal("-1372.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"फलटण ", "", "", "", new BigDecimal("2355.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"मिरकरवाडा ", "", "", "", new BigDecimal("90.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"काकडे शेठ ", "", "", "", new BigDecimal("738.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"कुंभारी (सोलापूर)", "", "", "", new BigDecimal("1552.50"), BigDecimal.ZERO, false, new BigDecimal("1170.00"), true, new BigDecimal("100.00"), false},
            {"संतोष शेठ (श्रीगोंदा)", "", "", "", BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"विश्रांतवाडी ", "", "", "", BigDecimal.ZERO, new BigDecimal("300.00"), true, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"रोहन (फलटण)", "", "", "", BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("40.00"), false},
            {"सातारा", "", "", "", new BigDecimal("22403.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"श्रीगोंदा ", "", "", "", new BigDecimal("45758.30"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("40.00"), false},
            {"SA (बारामती)", "", "", "", new BigDecimal("38306.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"शितप (तळी )", "", "", "", new BigDecimal("-1986.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"धोत्रे (बारामती)", "", "", "", new BigDecimal("2997.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"( UB )", "", "", "", new BigDecimal("-4635.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"( PW )", "", "", "", new BigDecimal("5259.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"लोंढे शेठ ", "", "", "", new BigDecimal("-709.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), true},
            {"जॉन शेठ  ", "", "", "", new BigDecimal("-466.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"वसई ", "", "", "", new BigDecimal("-3710.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, false, new BigDecimal("100.00"), false},
            {"ठाणे ", "", "", "", new BigDecimal("-1369.00"), new BigDecimal("850.00"), true, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"गोरश्वनाथ ", "", "", "", new BigDecimal("2533.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"कदम (तळी )", "", "", "", new BigDecimal("2137.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"सिद्धू ", "", "", "", new BigDecimal("4280.00"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"गणेश (फलटण)", "", "", "", new BigDecimal("-568.40"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("40.00"), false},
            {"चव्हाण शेठ ", "", "", "", new BigDecimal("649.50"), BigDecimal.ZERO, false, BigDecimal.ZERO, true, new BigDecimal("100.00"), false},
            {"थिटे वस्ती", "", "", "", new BigDecimal("1445.00"), new BigDecimal("300.00"), true, BigDecimal.ZERO, true, new BigDecimal("100.00"), false}
        };

        for (Object[] item : list) {
            Customer c = new Customer((String)item[0], (String)item[1], (String)item[2], (String)item[3], (BigDecimal)item[4]);
            c.setPagar((BigDecimal)item[5]);
            c.setPagarEnabled((Boolean)item[6]);
            c.setFarak((BigDecimal)item[7]);
            c.setCommissionEnabled((Boolean)item[8]);
            c.setShareRate((BigDecimal)item[9]);
            c.setShare30ProfitOnly((Boolean)item[10]);
            customerRepository.save(c);
        }
    }
}
