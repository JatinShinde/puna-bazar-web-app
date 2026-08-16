package com.punabazar.service;

import com.punabazar.dto.WeeklyReceiptDTO;
import com.punabazar.model.Customer;
import com.punabazar.model.Transaction;
import com.punabazar.repository.CustomerRepository;
import com.punabazar.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeeklyReceiptService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public WeeklyReceiptService(CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<WeeklyReceiptDTO> getWeeklyReceipts(LocalDate refDate) {
        LocalDate date = refDate != null ? refDate : LocalDate.now();
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(DayOfWeek.SUNDAY);

        List<Customer> customers = customerRepository.findAll();
        List<WeeklyReceiptDTO> dtos = new ArrayList<>();

        DecimalFormat fmt = new DecimalFormat("#,##,##0");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Customer c : customers) {
            List<Transaction> txs = transactionRepository.findByCustomerIdOrderByTransactionDateDesc(c.getId());

            Map<DayOfWeek, BigDecimal> dayNetMap = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek day : DayOfWeek.values()) {
                dayNetMap.put(day, BigDecimal.ZERO);
            }

            if (txs != null) {
                for (Transaction tx : txs) {
                    LocalDate txDate = tx.getTransactionDate() != null ? tx.getTransactionDate() :
                            (tx.getCreatedAt() != null ? tx.getCreatedAt().toLocalDate() : null);

                    if (txDate == null || (!txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek))) {
                        BigDecimal dailyNet = computeTransactionDailyNet(c, tx);
                        DayOfWeek dow = txDate != null ? txDate.getDayOfWeek() : DayOfWeek.MONDAY;
                        dayNetMap.put(dow, dayNetMap.get(dow).add(dailyNet));
                    }
                }
            }

            BigDecimal mon = dayNetMap.get(DayOfWeek.MONDAY);
            BigDecimal tue = dayNetMap.get(DayOfWeek.TUESDAY);
            BigDecimal wed = dayNetMap.get(DayOfWeek.WEDNESDAY);
            BigDecimal thu = dayNetMap.get(DayOfWeek.THURSDAY);
            BigDecimal fri = dayNetMap.get(DayOfWeek.FRIDAY);
            BigDecimal sat = dayNetMap.get(DayOfWeek.SATURDAY);
            BigDecimal sun = dayNetMap.get(DayOfWeek.SUNDAY);

            BigDecimal weeklyTotal = mon.add(tue).add(wed).add(thu).add(fri).add(sat).add(sun);
            String status = weeklyTotal.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

            StringBuilder sb = new StringBuilder();
            sb.append("==================================\n");
            sb.append("      *WEEKLY MARKET STATEMENT*\n");
            sb.append("      *").append(c.getName().toUpperCase()).append("*\n");
            sb.append("      *(").append(startOfWeek.format(dateFmt)).append(" - ").append(endOfWeek.format(dateFmt)).append(")*\n");
            sb.append("==================================\n");
            sb.append("*DAY*                   *NET TRADE*\n");
            sb.append("----------------------------------\n");
            sb.append("Mon :-               ").append(formatDayAmount(mon, fmt)).append("\n");
            sb.append("Tue :-               ").append(formatDayAmount(tue, fmt)).append("\n");
            sb.append("Wed :-               ").append(formatDayAmount(wed, fmt)).append("\n");
            sb.append("Thu :-               ").append(formatDayAmount(thu, fmt)).append("\n");
            sb.append("Fri :-               ").append(formatDayAmount(fri, fmt)).append("\n");
            sb.append("Sat :-               ").append(formatDayAmount(sat, fmt)).append("\n");
            sb.append("Sun :-               ").append(formatDayAmount(sun, fmt)).append("\n");
            sb.append("----------------------------------\n");
            sb.append("*WEEKLY TOTAL :-*     *").append(fmt.format(weeklyTotal.abs())).append(" ").append(status.toLowerCase()).append("*\n");
            sb.append("==================================\n");
            sb.append("TOTAL BALANCE DUE ").append(fmt.format(weeklyTotal.abs())).append(" ").append(status.toLowerCase());

            WeeklyReceiptDTO dto = new WeeklyReceiptDTO(
                    c.getId(),
                    c.getName(),
                    c.getCity(),
                    c.getMarketZone(),
                    mon, tue, wed, thu, fri, sat, sun,
                    weeklyTotal,
                    status,
                    sb.toString()
            );
            dtos.add(dto);
        }

        return dtos;
    }

    private BigDecimal computeTransactionDailyNet(Customer c, Transaction tx) {
        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
        BigDecimal totalSell = tx.getTotalSell() != null ? tx.getTotalSell() : sellPo.add(sellPc);

        BigDecimal payPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;
        BigDecimal totalPay = payPo.add(payPc);

        boolean isCommEnabled = c != null && Boolean.TRUE.equals(c.getCommissionEnabled());
        BigDecimal commRate = (c != null && c.getCommissionRate() != null) ? c.getCommissionRate() : new BigDecimal("10.00");
        BigDecimal commCut = isCommEnabled ? totalSell.multiply(commRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal totalAfterComm = totalSell.subtract(commCut);
        BigDecimal afterPay = totalAfterComm.subtract(totalPay);

        BigDecimal pagarVal = tx.getPagarAmount() != null ? tx.getPagarAmount() : (c != null && c.getPagar() != null ? c.getPagar() : BigDecimal.ZERO);
        BigDecimal farakVal = c != null && c.getFarak() != null ? c.getFarak() : BigDecimal.ZERO;

        BigDecimal runningNet = afterPay.subtract(pagarVal).subtract(farakVal);

        BigDecimal shareRate = c != null && c.getShareRate() != null ? c.getShareRate() : new BigDecimal("100.00");
        boolean isShareEnabled = shareRate.compareTo(new BigDecimal("100.00")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0;
        boolean is30ProfitOnly = c != null && (Boolean.TRUE.equals(c.getShare30ProfitOnly()) || shareRate.compareTo(new BigDecimal("30.00")) == 0);

        BigDecimal shareAmount = BigDecimal.ZERO;
        if (isShareEnabled) {
            if (is30ProfitOnly) {
                if (runningNet.compareTo(BigDecimal.ZERO) > 0) {
                    shareAmount = runningNet.multiply(shareRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            } else {
                shareAmount = runningNet.multiply(shareRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }

        return runningNet.subtract(shareAmount);
    }

    private String formatDayAmount(BigDecimal val, DecimalFormat fmt) {
        if (val == null || val.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }
        String suffix = val.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
        return fmt.format(val.abs()) + " " + suffix;
    }
}
