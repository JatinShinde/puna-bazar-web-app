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
            Map<DayOfWeek, BigDecimal> daySellMap = new EnumMap<>(DayOfWeek.class);
            Map<DayOfWeek, BigDecimal> dayPayMap = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek day : DayOfWeek.values()) {
                dayNetMap.put(day, BigDecimal.ZERO);
                daySellMap.put(day, BigDecimal.ZERO);
                dayPayMap.put(day, BigDecimal.ZERO);
            }

            if (txs != null) {
                for (Transaction tx : txs) {
                    LocalDate txDate = tx.getTransactionDate() != null ? tx.getTransactionDate() :
                            (tx.getCreatedAt() != null ? tx.getCreatedAt().toLocalDate() : null);

                    if (txDate != null && !txDate.isBefore(startOfWeek) && !txDate.isAfter(endOfWeek)) {
                        BigDecimal dailyNet = computeTransactionDailyNet(c, tx);
                        DayOfWeek dow = txDate.getDayOfWeek();
                        dayNetMap.put(dow, dayNetMap.get(dow).add(dailyNet));

                        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
                        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
                        BigDecimal totalSell = tx.getTotalSell() != null ? tx.getTotalSell() : sellPo.add(sellPc);

                        BigDecimal payPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
                        BigDecimal payPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;
                        BigDecimal totalPay = payPo.add(payPc);

                        daySellMap.put(dow, daySellMap.get(dow).add(totalSell));
                        dayPayMap.put(dow, dayPayMap.get(dow).add(totalPay));
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

            boolean isDetailedMarket = isDetailedWeeklyCustomer(c);

            StringBuilder sb = new StringBuilder();

            if (isDetailedMarket) {
                BigDecimal totalSellSum = BigDecimal.ZERO;
                BigDecimal totalPaySum = BigDecimal.ZERO;
                for (DayOfWeek d : DayOfWeek.values()) {
                    totalSellSum = totalSellSum.add(daySellMap.get(d));
                    totalPaySum = totalPaySum.add(dayPayMap.get(d));
                }

                BigDecimal netAfterPayment = totalSellSum.subtract(totalPaySum);

                boolean isCommEnabled = Boolean.TRUE.equals(c.getWeeklyCommissionEnabled()) || (c.getWeeklyCommissionRate() != null && c.getWeeklyCommissionRate().compareTo(BigDecimal.ZERO) > 0);
                BigDecimal commRatePct = (c.getWeeklyCommissionRate() != null && c.getWeeklyCommissionRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyCommissionRate() : BigDecimal.ZERO;
                BigDecimal commVal = (isCommEnabled && commRatePct.compareTo(BigDecimal.ZERO) > 0) ? totalSellSum.multiply(commRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal netAfterComm = netAfterPayment.subtract(commVal);

                boolean isPagarEnabled = Boolean.TRUE.equals(c.getWeeklyPagarEnabled()) || (c.getWeeklyPagar() != null && c.getWeeklyPagar().compareTo(BigDecimal.ZERO) > 0);
                BigDecimal pagarVal = (isPagarEnabled && c.getWeeklyPagar() != null) ? c.getWeeklyPagar() : BigDecimal.ZERO;
                BigDecimal netAfterPagar = netAfterComm.subtract(pagarVal);

                boolean isProfitOnlyShare = Boolean.TRUE.equals(c.getWeeklyShare30ProfitOnly());
                BigDecimal profitOnlyRatePct = (c.getWeeklyShare30ProfitOnlyRate() != null && c.getWeeklyShare30ProfitOnlyRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyShare30ProfitOnlyRate() : new BigDecimal("30.00");

                boolean isShareEnabled = Boolean.TRUE.equals(c.getWeeklyShareEnabled());
                BigDecimal shareRatePct = (c.getWeeklyShareRate() != null && c.getWeeklyShareRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyShareRate() : new BigDecimal("40.00");

                BigDecimal shareVal = BigDecimal.ZERO;
                BigDecimal displayShareRatePct = BigDecimal.ZERO;

                if (isProfitOnlyShare) {
                    displayShareRatePct = profitOnlyRatePct;
                    if (netAfterPagar.compareTo(BigDecimal.ZERO) > 0) {
                        shareVal = netAfterPagar.multiply(profitOnlyRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                    }
                } else if (isShareEnabled) {
                    displayShareRatePct = shareRatePct;
                    shareVal = netAfterPagar.multiply(shareRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                }

                boolean isAnyShareApplied = (isProfitOnlyShare || isShareEnabled) && shareVal.compareTo(BigDecimal.ZERO) > 0;
                BigDecimal netAfterShare = isAnyShareApplied ? netAfterPagar.subtract(shareVal) : netAfterPagar;

                BigDecimal mpAmount = c.getFarak() != null ? c.getFarak() : BigDecimal.ZERO;
                BigDecimal netAfterMp = netAfterShare.subtract(mpAmount);

                BigDecimal magilYeneVal = c.getYene() != null ? c.getYene() : BigDecimal.ZERO;
                BigDecimal magilDeneVal = c.getDene() != null ? c.getDene() : BigDecimal.ZERO;
                BigDecimal magilNet = magilYeneVal.subtract(magilDeneVal);

                BigDecimal finalNet = netAfterMp.add(magilNet);
                String finalStatus = finalNet.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

                sb.append("*DATE: ").append(startOfWeek.format(dateFmt)).append(" - ").append(endOfWeek.format(dateFmt)).append("*\n");
                sb.append("                      *SELL*     *PAYMENT*\n");
                sb.append("----------------------------------\n");

                DayOfWeek[] days = DayOfWeek.values();
                for (DayOfWeek d : days) {
                    String dName = d.name().substring(0, 3);
                    BigDecimal sVal = daySellMap.get(d);
                    BigDecimal pVal = dayPayMap.get(d);
                    sb.append(String.format("*%s :-*              %6s     %6s\n", dName, fmt.format(sVal), fmt.format(pVal)));
                }

                sb.append("----------------------------------\n");
                sb.append(String.format("*TOTAL :-*            %6s     %6s\n", fmt.format(totalSellSum), fmt.format(totalPaySum)));
                if (totalPaySum.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(String.format("*- PAYMENT :-*                   -%6s\n", fmt.format(totalPaySum)));
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*TOTAL :-*            %6s\n", fmt.format(netAfterPayment)));
                }

                if (isCommEnabled && commVal.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*COM (%s%%) :-*                    -%6s\n", commRatePct.stripTrailingZeros().toPlainString(), fmt.format(commVal)));
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*TOTAL :-*            %6s\n", fmt.format(netAfterComm)));
                }

                if (isPagarEnabled && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*PAGAR :-*                        -%6s\n", fmt.format(pagarVal)));
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*TOTAL :-*            %6s\n", fmt.format(netAfterPagar)));
                }

                if (isAnyShareApplied && shareVal.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*(%s%%) :-*                       -%6s\n", displayShareRatePct.stripTrailingZeros().toPlainString(), fmt.format(shareVal)));
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*TOTAL :-*            %6s %s\n", fmt.format(netAfterShare.abs()), netAfterShare.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene"));
                }

                if (mpAmount.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*MISS PAYMENT :-*                 -%6s\n", fmt.format(mpAmount)));
                    sb.append("----------------------------------\n");
                    sb.append(String.format("*TOTAL :-*            %6s %s\n", fmt.format(netAfterMp.abs()), netAfterMp.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene"));
                }

                if (magilYeneVal.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append("🔴 *MAGIL YENE :-*                +").append(fmt.format(magilYeneVal)).append(" yeṇe\n");
                } else if (magilDeneVal.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append("----------------------------------\n");
                    sb.append("🔴 *MAGIL DENE :-*                -").append(fmt.format(magilDeneVal)).append(" dene\n");
                }

                sb.append("==================================\n");
                sb.append("TOTAL BALANCE DUE ").append(fmt.format(finalNet.abs())).append(" ").append(finalStatus.toLowerCase());

                WeeklyReceiptDTO dto = new WeeklyReceiptDTO(
                        c.getId(),
                        c.getName(),
                        c.getCity(),
                        c.getMarketZone(),
                        mon, tue, wed, thu, fri, sat, sun,
                        netAfterPayment,
                        netAfterPayment.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE",
                        finalNet,
                        finalStatus,
                        false,
                        BigDecimal.ZERO,
                        shareRatePct,
                        sb.toString()
                );
                dtos.add(dto);
                continue;
            }

            BigDecimal weeklyTotal = mon.add(tue).add(wed).add(thu).add(fri).add(sat).add(sun);
            String rawStatus = weeklyTotal.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

            BigDecimal currentBal = weeklyTotal;

            // Step B: Weekly Commission (if enabled)
            boolean isCommEnabled = Boolean.TRUE.equals(c.getWeeklyCommissionEnabled()) || (c.getWeeklyCommissionRate() != null && c.getWeeklyCommissionRate().compareTo(BigDecimal.ZERO) > 0);
            BigDecimal commRatePct = (c.getWeeklyCommissionRate() != null && c.getWeeklyCommissionRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyCommissionRate() : BigDecimal.ZERO;
            BigDecimal commVal = (isCommEnabled && commRatePct.compareTo(BigDecimal.ZERO) > 0) ? currentBal.abs().multiply(commRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal afterCommSum = currentBal;
            if (commVal.compareTo(BigDecimal.ZERO) > 0) {
                afterCommSum = currentBal.compareTo(BigDecimal.ZERO) >= 0 ? currentBal.subtract(commVal) : currentBal.add(commVal);
                currentBal = afterCommSum;
            }

            // Step C: Pagar (if enabled)
            boolean isPagarEnabled = Boolean.TRUE.equals(c.getWeeklyPagarEnabled()) || (c.getWeeklyPagar() != null && c.getWeeklyPagar().compareTo(BigDecimal.ZERO) > 0);
            BigDecimal pagarVal = (isPagarEnabled && c.getWeeklyPagar() != null) ? c.getWeeklyPagar() : BigDecimal.ZERO;
            BigDecimal afterPagarSum = currentBal;
            if (pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                afterPagarSum = currentBal.subtract(pagarVal);
                currentBal = afterPagarSum;
            }

            // Step D: Weekly Share % / Profit-Only
            boolean isProfitOnlyShare = Boolean.TRUE.equals(c.getWeeklyShare30ProfitOnly());
            BigDecimal profitOnlyRatePct = (c.getWeeklyShare30ProfitOnlyRate() != null && c.getWeeklyShare30ProfitOnlyRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyShare30ProfitOnlyRate() : new BigDecimal("30.00");

            boolean isShareEnabled = Boolean.TRUE.equals(c.getWeeklyShareEnabled());
            BigDecimal shareRatePct = (c.getWeeklyShareRate() != null && c.getWeeklyShareRate().compareTo(BigDecimal.ZERO) > 0) ? c.getWeeklyShareRate() : new BigDecimal("40.00");

            BigDecimal shareVal = BigDecimal.ZERO;
            BigDecimal displayShareRatePct = BigDecimal.ZERO;

            if (isProfitOnlyShare) {
                displayShareRatePct = profitOnlyRatePct;
                if (currentBal.compareTo(BigDecimal.ZERO) > 0) {
                    shareVal = currentBal.multiply(profitOnlyRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
                }
            } else if (isShareEnabled) {
                displayShareRatePct = shareRatePct;
                shareVal = currentBal.multiply(shareRatePct).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            }

            boolean isAnyShareApplied = (isProfitOnlyShare || isShareEnabled) && shareVal.compareTo(BigDecimal.ZERO) > 0;
            BigDecimal afterShareSum = isAnyShareApplied ? currentBal.subtract(shareVal) : currentBal;
            currentBal = afterShareSum;

            // Step E: MP (Miss Payment / Farak)
            BigDecimal mpAmount = c.getFarak() != null ? c.getFarak() : BigDecimal.ZERO;
            BigDecimal afterMpSum = currentBal;
            if (mpAmount.compareTo(BigDecimal.ZERO) > 0) {
                afterMpSum = currentBal.subtract(mpAmount);
                currentBal = afterMpSum;
            }

            // Step F: Magil Yene / Dene
            BigDecimal magilYeneVal = c.getYene() != null ? c.getYene() : BigDecimal.ZERO;
            BigDecimal magilDeneVal = c.getDene() != null ? c.getDene() : BigDecimal.ZERO;
            BigDecimal magilNet = magilYeneVal.subtract(magilDeneVal);
            BigDecimal finalNet = currentBal.add(magilNet);
            String finalStatus = finalNet.compareTo(BigDecimal.ZERO) >= 0 ? "YENE" : "DENE";

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

            if (commVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*COM (").append(commRatePct.stripTrailingZeros().toPlainString()).append("%) :-*             -").append(fmt.format(commVal)).append("\n");
                sb.append("----------------------------------\n");
                String afterCommStatus = afterCommSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*TOTAL :-*           ").append(fmt.format(afterCommSum.abs())).append(" ").append(afterCommStatus).append("\n");
            }

            if (pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*PAGAR :-*           -").append(fmt.format(pagarVal)).append("\n");
                sb.append("----------------------------------\n");
                String afterPagarStatus = afterPagarSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*TOTAL :-*           ").append(fmt.format(afterPagarSum.abs())).append(" ").append(afterPagarStatus).append("\n");
            }

            if (isAnyShareApplied && shareVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                String afterShareStatus = afterShareSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*(").append(displayShareRatePct.stripTrailingZeros().toPlainString()).append("%) :-*             -").append(fmt.format(shareVal)).append("\n");
                sb.append("----------------------------------\n");
                sb.append("*TOTAL :-*           ").append(fmt.format(afterShareSum.abs())).append(" ").append(afterShareStatus).append("\n");
            }

            if (mpAmount.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("*MISS PAYMENT :-*    -").append(fmt.format(mpAmount)).append("\n");
                sb.append("----------------------------------\n");
                String afterMpStatus = afterMpSum.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
                sb.append("*TOTAL :-*           ").append(fmt.format(afterMpSum.abs())).append(" ").append(afterMpStatus).append("\n");
            }

            if (magilYeneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("🔴 *MAGIL YENE :-*   +").append(fmt.format(magilYeneVal)).append(" yeṇe\n");
            } else if (magilDeneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("----------------------------------\n");
                sb.append("🔴 *MAGIL DENE :-*   -").append(fmt.format(magilDeneVal)).append(" dene\n");
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

    private boolean isDetailedWeeklyCustomer(Customer c) {
        if (c == null) return false;
        String name = (c.getName() != null ? c.getName() : "").toLowerCase();
        String city = (c.getCity() != null ? c.getCity() : "").toLowerCase();
        String zone = (c.getMarketZone() != null ? c.getMarketZone() : "").toLowerCase();
        String style = (c.getReceiptStyle() != null ? c.getReceiptStyle() : "").toLowerCase();

        return name.contains("जाणवलकर") || name.contains("janvalkar") || name.contains("zanvalkar") || name.contains("वसई") || name.contains("vasai")
                || city.contains("जाणवलकर") || city.contains("janvalkar") || city.contains("zanvalkar") || city.contains("वसई") || city.contains("vasai")
                || zone.contains("जाणवलकर") || zone.contains("janvalkar") || zone.contains("zanvalkar") || zone.contains("वसई") || zone.contains("vasai")
                || style.contains("detailed") || style.contains("type_2");
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
