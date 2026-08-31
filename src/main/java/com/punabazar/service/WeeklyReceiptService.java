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

                    if (txDate != null && !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)) {
                        BigDecimal dailyNet = computeTransactionDailyNet(c, tx);
                        DayOfWeek dow = txDate.getDayOfWeek();
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
            String rawStatus = weeklyTotal.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

            BigDecimal currentBal = weeklyTotal;

            // Step B: MP (Miss Payment / Farak)
            BigDecimal mpAmount = c.getFarak() != null ? c.getFarak() : BigDecimal.ZERO;
            BigDecimal afterMpSum = currentBal;
            if (mpAmount.compareTo(BigDecimal.ZERO) > 0) {
                afterMpSum = currentBal.compareTo(BigDecimal.ZERO) >= 0 ? currentBal.subtract(mpAmount) : currentBal.add(mpAmount);
                currentBal = afterMpSum;
            }

            // Step C: Pagar
            boolean isPagarEnabled = Boolean.TRUE.equals(c.getPagarEnabled());
            BigDecimal pagarVal = (isPagarEnabled && c.getPagar() != null) ? c.getPagar() : BigDecimal.ZERO;
            BigDecimal afterPagarSum = currentBal;
            if (pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                afterPagarSum = currentBal.subtract(pagarVal);
                currentBal = afterPagarSum;
            }

            // Step D: Share %
            BigDecimal shareRatePct = c.getShareRate() != null ? c.getShareRate() : new BigDecimal("100.00");
            BigDecimal shareVal = BigDecimal.ZERO;
            BigDecimal afterShareSum = currentBal;
            if (shareRatePct.compareTo(BigDecimal.ZERO) > 0 && shareRatePct.compareTo(new BigDecimal("100.00")) < 0) {
                shareVal = currentBal.abs().multiply(shareRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                afterShareSum = currentBal.compareTo(BigDecimal.ZERO) >= 0 ? shareVal : shareVal.negate();
                currentBal = afterShareSum;
            }

            // Step E: Magil Yene / Dene
            BigDecimal magilYeneVal = c.getYene() != null ? c.getYene() : BigDecimal.ZERO;
            BigDecimal magilDeneVal = c.getDene() != null ? c.getDene() : BigDecimal.ZERO;
            BigDecimal magilNet = magilYeneVal.subtract(magilDeneVal);
            BigDecimal finalNet = currentBal.add(magilNet);
            String finalStatus = finalNet.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

            StringBuilder sb = new StringBuilder();
            sb.append("*DATE: ").append(startOfWeek.format(dateFmt)).append(" - ").append(endOfWeek.format(dateFmt)).append("*\n");
            sb.append("Mon :-               ").append(formatDayAmount(mon, fmt)).append("\n");
            sb.append("Tue :-               ").append(formatDayAmount(tue, fmt)).append("\n");
            sb.append("Wed :-               ").append(formatDayAmount(wed, fmt)).append("\n");
            sb.append("Thu :-               ").append(formatDayAmount(thu, fmt)).append("\n");
            sb.append("Fri :-               ").append(formatDayAmount(fri, fmt)).append("\n");
            sb.append("Sat :-               ").append(formatDayAmount(sat, fmt)).append("\n");
            sb.append("Sun :-               ").append(formatDayAmount(sun, fmt)).append("\n");
            sb.append("----------------------------------\n");
            sb.append("*7-DAY TOTAL :-*     ").append(fmt.format(weeklyTotal.abs())).append(" ").append(rawStatus.toLowerCase()).append("\n");

            if (mpAmount.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*MP :-*               ").append(fmt.format(mpAmount)).append("\n");
                sb.append("----------------------------------\n");
                String afterMpStatus = afterMpSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*AFTER MP :-*        ").append(fmt.format(afterMpSum.abs())).append(" ").append(afterMpStatus).append("\n");
            }

            if (pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*PAGAR :-*           -").append(fmt.format(pagarVal)).append("\n");
                sb.append("----------------------------------\n");
                String afterPagarStatus = afterPagarSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*AFTER PAGAR :-*     ").append(fmt.format(afterPagarSum.abs())).append(" ").append(afterPagarStatus).append("\n");
            }

            if (shareRatePct.compareTo(BigDecimal.ZERO) > 0 && shareRatePct.compareTo(new BigDecimal("100.00")) < 0) {
                sb.append("----------------------------------\n");
                String afterShareStatus = afterShareSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*(").append(shareRatePct.stripTrailingZeros().toPlainString()).append("%) :-*             ").append(fmt.format(shareVal)).append(" ").append(afterShareStatus).append("\n");
            }

            if (magilYeneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*MA YENE :-*         +").append(fmt.format(magilYeneVal)).append("\n");
            } else if (magilDeneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*MA DENE :-*         -").append(fmt.format(magilDeneVal)).append("\n");
            }

            sb.append("==================================\n");
            sb.append("*WEEKLY TOTAL :-*    ").append(fmt.format(finalNet.abs())).append(" ").append(finalStatus.toLowerCase()).append("\n");
            sb.append("==================================\n");
            sb.append("TOTAL BALANCE DUE ").append(fmt.format(finalNet.abs())).append(" ").append(finalStatus.toLowerCase());

            WeeklyReceiptDTO dto = new WeeklyReceiptDTO(
                    c.getId(),
                    c.getName(),
                    c.getCity(),
                    c.getMarketZone(),
                    mon, tue, wed, thu, fri, sat, sun,
                    weeklyTotal,
                    rawStatus,
                    finalNet,
                    finalStatus,
                    isPagarEnabled,
                    pagarVal,
                    shareRatePct,
                    sb.toString()
            );
            dtos.add(dto);
        }

        return dtos;
    }

    private BigDecimal computeTransactionDailyNet(Customer c, Transaction tx) {
        return WhatsAppService.calculateReceiptTodayNet(c, tx, true, false);
    }

    private String formatDayAmount(BigDecimal val, DecimalFormat fmt) {
        if (val == null || val.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }
        String suffix = val.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
        return fmt.format(val.abs()) + " " + suffix;
    }
}
