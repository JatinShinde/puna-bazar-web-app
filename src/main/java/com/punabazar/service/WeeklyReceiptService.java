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

            BigDecimal weeklySubtotal = mon.add(tue).add(wed).add(thu).add(fri).add(sat).add(sun);
            BigDecimal farakVal = (c.getFarak() != null && c.getFarak().compareTo(BigDecimal.ZERO) != 0) ? c.getFarak() : BigDecimal.ZERO;
            BigDecimal weeklyTotal = weeklySubtotal.subtract(farakVal);
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

            if (farakVal.compareTo(BigDecimal.ZERO) != 0) {
                sb.append("*MISS PAYMENT :-*     ").append(fmt.format(farakVal.abs())).append("\n");
                sb.append("----------------------------------\n");
            }

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
        return WhatsAppService.calculateReceiptTodayNet(c, tx, false, true);
    }

    private String formatDayAmount(BigDecimal val, DecimalFormat fmt) {
        if (val == null || val.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }
        String suffix = val.compareTo(BigDecimal.ZERO) >= 0 ? "yeṇe" : "dene";
        return fmt.format(val.abs()) + " " + suffix;
    }
}
