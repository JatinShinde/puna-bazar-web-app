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
        return getAllCustomers(null);
    }

    public List<Customer> getAllCustomers(LocalDate targetDate) {
        if (customerRepository.count() == 0) {
            Object[][] listData = new Object[][] {
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

            for (Object[] item : listData) {
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
        List<Customer> list = customerRepository.findAll();
        populateTodayNet(list, targetDate);
        return list;
    }

    public Customer getCustomerById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        populateTodayNet(java.util.Collections.singletonList(c), null);
        return c;
    }

    public List<Customer> searchCustomers(String query) {
        return searchCustomers(query, null);
    }

    public List<Customer> searchCustomers(String query, LocalDate targetDate) {
        List<Customer> list;
        if (query == null || query.trim().isEmpty()) {
            list = customerRepository.findAll();
        } else {
            list = customerRepository.searchCustomers(query.trim());
        }
        populateTodayNet(list, targetDate);
        return list;
    }

    public List<Customer> getCustomersByCity(String city) {
        return getCustomersByCity(city, null);
    }

    public List<Customer> getCustomersByCity(String city, LocalDate targetDate) {
        List<Customer> list = customerRepository.findByCityIgnoreCase(city);
        populateTodayNet(list, targetDate);
        return list;
    }

    private void populateTodayNet(List<Customer> list, LocalDate targetDate) {
        if (list == null || list.isEmpty()) return;
        try {
            LocalDate dateToUse = targetDate != null ? targetDate : LocalDate.now();
            List<Transaction> todayTxs = transactionRepository.findByTransactionDate(dateToUse);
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

    @Transactional
    public void seedDefaultLiveCustomers() {
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
