package com.punabazar.service;

import com.punabazar.dto.WhatsAppStatementDTO;
import com.punabazar.model.Customer;
import com.punabazar.model.Ledger;
import com.punabazar.model.Transaction;

import com.punabazar.repository.CustomerRepository;
import com.punabazar.repository.LedgerRepository;
import com.punabazar.repository.TransactionRepository;
import com.punabazar.repository.WhatsAppTemplateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;

@Service
public class WhatsAppService {

    private final CustomerRepository customerRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionRepository transactionRepository;
    private final WhatsAppTemplateRepository whatsappTemplateRepository;

    public WhatsAppService(CustomerRepository customerRepository, LedgerRepository ledgerRepository, TransactionRepository transactionRepository, WhatsAppTemplateRepository whatsappTemplateRepository) {
        this.customerRepository = customerRepository;
        this.ledgerRepository = ledgerRepository;
        this.transactionRepository = transactionRepository;
        this.whatsappTemplateRepository = whatsappTemplateRepository;
    }

    public WhatsAppStatementDTO generateStatement(Long customerId) {
        return generateStatement(customerId, null, null, null, null, null, null, null, null);
    }

    public WhatsAppStatementDTO generateStatement(Long customerId, String styleOverride) {
        return generateStatement(customerId, styleOverride, null, null, null, null, null, null, null, null);
    }

    public WhatsAppStatementDTO generateStatement(Long customerId, String styleOverride, BigDecimal yeneParam, BigDecimal deneParam, BigDecimal farakParam) {
        return generateStatement(customerId, styleOverride, null, null, null, null, farakParam, null, yeneParam, deneParam);
    }

    public WhatsAppStatementDTO generateStatement(Long customerId, String styleOverride,
                                                BigDecimal sellPoParam, BigDecimal sellPcParam,
                                                BigDecimal payPoParam, BigDecimal payPcParam,
                                                BigDecimal farakParam, BigDecimal yeneParam, BigDecimal deneParam) {
        return generateStatement(customerId, styleOverride, sellPoParam, sellPcParam, payPoParam, payPcParam, farakParam, null, yeneParam, deneParam);
    }

    public WhatsAppStatementDTO generateStatement(Long customerId, String styleOverride,
                                                BigDecimal sellPoParam, BigDecimal sellPcParam,
                                                BigDecimal payPoParam, BigDecimal payPcParam,
                                                BigDecimal farakParam, BigDecimal pagarParam,
                                                BigDecimal yeneParam, BigDecimal deneParam) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        List<Ledger> ledgers = ledgerRepository.findByCustomerIdOrderByEntryDateDesc(customerId);
        Ledger latestLedger = ledgers.isEmpty() ? null : ledgers.get(0);

        List<Transaction> txs = transactionRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
        Transaction latestTx = txs.isEmpty() ? null : txs.get(0);

        String rawCity = customer.getCity() != null && !customer.getCity().trim().isEmpty() 
                ? customer.getCity().trim().toUpperCase() 
                : "PUNE";

        boolean isFormTrade = (sellPoParam != null || sellPcParam != null || payPoParam != null || payPcParam != null);

        BigDecimal sellPoVal = BigDecimal.ZERO;
        BigDecimal sellPcVal = BigDecimal.ZERO;
        BigDecimal payPoVal = BigDecimal.ZERO;
        BigDecimal payPcVal = BigDecimal.ZERO;

        if (isFormTrade) {
            sellPoVal = sellPoParam != null ? sellPoParam : BigDecimal.ZERO;
            sellPcVal = sellPcParam != null ? sellPcParam : BigDecimal.ZERO;
            payPoVal = payPoParam != null ? payPoParam : BigDecimal.ZERO;
            payPcVal = payPcParam != null ? payPcParam : BigDecimal.ZERO;
        } else if (latestTx != null) {
            sellPoVal = latestTx.getSellPo() != null ? latestTx.getSellPo() : BigDecimal.ZERO;
            sellPcVal = latestTx.getSellPcAmount() != null ? latestTx.getSellPcAmount() : BigDecimal.ZERO;
            payPoVal = latestTx.getPaymentPo() != null ? latestTx.getPaymentPo() : BigDecimal.ZERO;
            payPcVal = latestTx.getPaymentPc() != null ? latestTx.getPaymentPc() : BigDecimal.ZERO;
        }

        BigDecimal farakVal = BigDecimal.ZERO;
        if (farakParam != null && farakParam.compareTo(BigDecimal.ZERO) != 0) {
            farakVal = farakParam;
        } else if (customer.getFarak() != null && customer.getFarak().compareTo(BigDecimal.ZERO) != 0) {
            farakVal = customer.getFarak();
        }

        BigDecimal pagarVal = BigDecimal.ZERO;
        if (pagarParam != null) {
            pagarVal = pagarParam;
        } else if (latestTx != null && latestTx.getPagarAmount() != null) {
            pagarVal = latestTx.getPagarAmount();
        } else if (customer.getPagar() != null) {
            pagarVal = customer.getPagar();
        }

        BigDecimal totalSellVal = sellPoVal.add(sellPcVal);
        BigDecimal totalPaymentVal = payPoVal.add(payPcVal);

        BigDecimal commRateVal = customer.getCommissionRate() != null ? customer.getCommissionRate() : new BigDecimal("10.00");
        
        // Commission % is ONLY calculated when there is profit (totalSellVal > totalPaymentVal / YENE)
        BigDecimal commAmountVal = BigDecimal.ZERO;
        if (totalSellVal.compareTo(totalPaymentVal) > 0) {
            commAmountVal = totalSellVal.multiply(commRateVal).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        }
        BigDecimal totalAfterCommVal = totalSellVal.subtract(commAmountVal);
        BigDecimal afterPayVal = totalAfterCommVal.subtract(totalPaymentVal);

        BigDecimal yeneVal = BigDecimal.ZERO;
        BigDecimal deneVal = BigDecimal.ZERO;

        if (yeneParam != null) {
            yeneVal = yeneParam;
        }
        if (deneParam != null) {
            deneVal = deneParam;
        }

        if (yeneParam == null && deneParam == null) {
            if (latestTx != null && latestTx.getMagilBaki() != null && latestTx.getMagilBaki().compareTo(BigDecimal.ZERO) != 0) {
                if (latestTx.getMagilBaki().compareTo(BigDecimal.ZERO) > 0) {
                    yeneVal = latestTx.getMagilBaki();
                } else {
                    deneVal = latestTx.getMagilBaki().abs();
                }
            } else if (customer.getYene() != null && customer.getYene().compareTo(BigDecimal.ZERO) > 0) {
                yeneVal = customer.getYene();
            } else if (customer.getDene() != null && customer.getDene().compareTo(BigDecimal.ZERO) > 0) {
                deneVal = customer.getDene();
            } else if (customer.getPreviousBalance() != null) {
                if (customer.getPreviousBalance().compareTo(BigDecimal.ZERO) > 0) {
                    yeneVal = customer.getPreviousBalance();
                } else if (customer.getPreviousBalance().compareTo(BigDecimal.ZERO) < 0) {
                    deneVal = customer.getPreviousBalance().abs();
                }
            }
        }

        BigDecimal netOpening = yeneVal.subtract(deneVal);

        String receiptStyle = "TYPE_1";
        if (styleOverride != null && !styleOverride.trim().isEmpty()) {
            receiptStyle = styleOverride.trim().toUpperCase();
        } else if (customer.getReceiptStyle() != null && !customer.getReceiptStyle().trim().isEmpty()) {
            receiptStyle = customer.getReceiptStyle().trim().toUpperCase();
        }

        if ("STANDARD".equals(receiptStyle) || "FARAK_SHARE".equals(receiptStyle) || "TYPE1".equals(receiptStyle)) {
            receiptStyle = "TYPE_1";
        } else if ("SIMPLE".equals(receiptStyle) || "TYPE2".equals(receiptStyle)) {
            receiptStyle = "TYPE_2";
        } else if ("TYPE3".equals(receiptStyle)) {
            receiptStyle = "TYPE_3";
        } else if ("TYPE4".equals(receiptStyle)) {
            receiptStyle = "TYPE_4";
        } else if ("SHARE_PERCENT".equals(receiptStyle) || "TYPE5".equals(receiptStyle)) {
            receiptStyle = "TYPE_5";
        }

        BigDecimal shareRate = customer.getShareRate() != null ? customer.getShareRate() : new BigDecimal("100.00");

        String dateStr = "";
        if (latestTx != null && latestTx.getTransactionDate() != null) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy");
            dateStr = latestTx.getTransactionDate().format(dtf);
        } else {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy");
            dateStr = java.time.LocalDate.now().format(dtf);
        }

        StringBuilder sb = new StringBuilder();
        BigDecimal netBalanceVal;

        if ("TYPE_1".equals(receiptStyle) || "FARAK_SHARE".equals(receiptStyle)) {
            String dateFormatted = dateStr;
            if (latestTx != null && latestTx.getTransactionDate() != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = latestTx.getTransactionDate().format(dtf);
            } else {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = java.time.LocalDate.now().format(dtf);
            }

            sb.append("*Date: ").append(dateFormatted).append("*\n");
            sb.append(formatCenteredCity(rawCity)).append("\n\n");
            sb.append(formatTableHeader()).append("\n");
            sb.append(formatTableRow("PO", sellPoVal, payPoVal)).append("\n");
            sb.append(formatTableRow("PC", sellPcVal, payPcVal)).append("\n");
            sb.append("-----------------------------------------------------\n");
            sb.append(formatTableRow("TOTAL", totalSellVal, totalPaymentVal)).append("\n");
            sb.append("-----------------------------------------------------\n");

            sb.append("*COM (").append(commRateVal.stripTrailingZeros().toPlainString()).append("%):-  ").append(fmtNum(commAmountVal)).append("*\n");
            sb.append("---------------------------------\n");
            sb.append("*TOTAL:-           ").append(fmtNum(totalAfterCommVal)).append("*\n");
            sb.append("*PAYMENT:-     ").append(fmtNum(totalPaymentVal)).append("*\n");
            sb.append("---------------------------------\n");

            BigDecimal runningNet1 = afterPayVal;
            if (pagarVal != null && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("*PAGAR:-        ").append(fmtNum(pagarVal)).append("*\n");
                sb.append("---------------------------------\n");
                runningNet1 = runningNet1.subtract(pagarVal);
            }

            sb.append("*REMAINING:-       ").append(fmtNum(runningNet1)).append("*\n");

            BigDecimal afterFarak = runningNet1;
            if (farakVal != null && farakVal.compareTo(BigDecimal.ZERO) != 0) {
                sb.append("---------------------------------\n");
                sb.append("*MISS PAYMENT:-    ").append(fmtNum(farakVal)).append("*\n");
                sb.append("---------------------------------\n");
                afterFarak = runningNet1.subtract(farakVal);
                sb.append("*TOTAL:-            ").append(fmtNum(afterFarak)).append("*\n");
            }

            boolean isShareEnabled = (shareRate != null && shareRate.compareTo(new BigDecimal("100")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0);
            BigDecimal shareAmount = BigDecimal.ZERO;
            BigDecimal todayNet = afterFarak;

            if (isShareEnabled) {
                shareAmount = afterFarak.multiply(shareRate).divide(new BigDecimal("100"));
                todayNet = afterFarak.subtract(shareAmount);
                sb.append("---------------------------------\n");
                sb.append("*(").append(shareRate.stripTrailingZeros().toPlainString()).append("%):-  ").append(fmtNum(shareAmount)).append("*\n");
                sb.append("---------------------------------\n");
            }

            netBalanceVal = todayNet.add(netOpening);
            appendBalanceSection(sb, todayNet, yeneVal, deneVal, netBalanceVal);
        } else if ("TYPE_2".equals(receiptStyle) || "SIMPLE".equals(receiptStyle)) {
            String dateFormatted = dateStr;
            if (latestTx != null && latestTx.getTransactionDate() != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = latestTx.getTransactionDate().format(dtf);
            } else {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = java.time.LocalDate.now().format(dtf);
            }

            sb.append("*Date: ").append(dateFormatted).append("*\n");
            sb.append(formatCenteredCity(rawCity)).append("\n\n");
            sb.append(formatTableHeader()).append("\n");
            sb.append(formatTableRow("PO", sellPoVal, payPoVal)).append("\n");
            sb.append(formatTableRow("PC", sellPcVal, payPcVal)).append("\n");
            sb.append("-----------------------------------------------------\n");
            sb.append(formatTableRow("TOTAL", totalSellVal, totalPaymentVal)).append("\n");
            sb.append("-----------------------------------------------------\n");

            sb.append("*PAYMENT:-     ").append(fmtNum(totalPaymentVal)).append("*\n");
            sb.append("---------------------------------\n");

            BigDecimal todayNet = totalSellVal.subtract(totalPaymentVal);
            if (pagarVal != null && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("*PAGAR:-        ").append(fmtNum(pagarVal)).append("*\n");
                sb.append("---------------------------------\n");
                todayNet = todayNet.subtract(pagarVal);
            }
            netBalanceVal = todayNet.add(netOpening);
            appendBalanceSection(sb, todayNet, yeneVal, deneVal, netBalanceVal);
        } else if ("TYPE_5".equals(receiptStyle) || "SHARE_PERCENT".equals(receiptStyle)) {
            String dateFormatted = dateStr;
            if (latestTx != null && latestTx.getTransactionDate() != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = latestTx.getTransactionDate().format(dtf);
            } else {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = java.time.LocalDate.now().format(dtf);
            }

            sb.append("*Date: ").append(dateFormatted).append("*\n");
            sb.append(formatCenteredCity(rawCity)).append("\n\n");
            sb.append(formatTableHeader()).append("\n");
            sb.append(formatTableRow("PO", sellPoVal, payPoVal)).append("\n");
            sb.append(formatTableRow("PC", sellPcVal, payPcVal)).append("\n");
            sb.append("-----------------------------------------------------\n");
            sb.append(formatTableRow("TOTAL", totalSellVal, totalPaymentVal)).append("\n");
            sb.append("-----------------------------------------------------\n");

            sb.append("*COM (").append(commRateVal.stripTrailingZeros().toPlainString()).append("%):-  ").append(fmtNum(commAmountVal)).append("*\n");
            sb.append("---------------------------------\n");
            sb.append("*TOTAL:-           ").append(fmtNum(totalAfterCommVal)).append("*\n");
            sb.append("*PAYMENT:-     ").append(fmtNum(totalPaymentVal)).append("*\n");
            sb.append("---------------------------------\n");

            BigDecimal runningNet5 = afterPayVal;
            if (pagarVal != null && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("*PAGAR:-        ").append(fmtNum(pagarVal)).append("*\n");
                sb.append("---------------------------------\n");
                runningNet5 = runningNet5.subtract(pagarVal);
            } else {
                sb.append("                          *").append(fmtNum(afterPayVal)).append("*\n");
            }

            boolean isShareEnabled = (shareRate != null && shareRate.compareTo(new BigDecimal("100")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0);
            BigDecimal shareAmount = BigDecimal.ZERO;
            BigDecimal todayNet = runningNet5;

            if (isShareEnabled) {
                shareAmount = runningNet5.multiply(shareRate).divide(new BigDecimal("100"));
                todayNet = runningNet5.subtract(shareAmount);
                sb.append("            *(").append(shareRate.stripTrailingZeros().toPlainString()).append("%):-  ").append(fmtNum(shareAmount)).append("*\n");
                sb.append("---------------------------------\n");
            }

            netBalanceVal = todayNet.add(netOpening);
            appendBalanceSection(sb, todayNet, yeneVal, deneVal, netBalanceVal);
        } else if ("TYPE_4".equals(receiptStyle)) {
            String dateFormatted = dateStr;
            if (latestTx != null && latestTx.getTransactionDate() != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = latestTx.getTransactionDate().format(dtf);
            } else {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = java.time.LocalDate.now().format(dtf);
            }

            sb.append("*Date: ").append(dateFormatted).append("*\n");
            sb.append(formatCenteredCity(rawCity)).append("\n\n");
            sb.append(formatTableHeader()).append("\n");
            sb.append(formatTableRow("PO", sellPoVal, payPoVal)).append("\n");
            sb.append(formatTableRow("PC", sellPcVal, payPcVal)).append("\n");
            sb.append("-----------------------------------------------------\n");
            sb.append(formatTableRow("TOTAL", totalSellVal, totalPaymentVal)).append("\n");
            sb.append("-----------------------------------------------------\n");

            sb.append("*PAYMENT:-     ").append(fmtNum(totalPaymentVal)).append("*\n");
            sb.append("---------------------------------\n");
            BigDecimal afterPayDirect = totalSellVal.subtract(totalPaymentVal);
            
            BigDecimal todayNet = afterPayDirect;
            if (pagarVal != null && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("                          *").append(fmtNum(afterPayDirect)).append("*\n");
                sb.append("*PAGAR:-        ").append(fmtNum(pagarVal)).append("*\n");
                sb.append("---------------------------------\n");
                todayNet = afterPayDirect.subtract(pagarVal);
            }

            netBalanceVal = todayNet.add(netOpening);
            appendBalanceSection(sb, todayNet, yeneVal, deneVal, netBalanceVal);
        } else {
            // TYPE_3 or STANDARD (PO, PC, Com, Pay, Pagar, Magil Baki)
            String dateFormatted = dateStr;
            if (latestTx != null && latestTx.getTransactionDate() != null) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = latestTx.getTransactionDate().format(dtf);
            } else {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateFormatted = java.time.LocalDate.now().format(dtf);
            }

            sb.append("*Date: ").append(dateFormatted).append("*\n");
            sb.append(formatCenteredCity(rawCity)).append("\n\n");
            sb.append(formatTableHeader()).append("\n");
            sb.append(formatTableRow("PO", sellPoVal, payPoVal)).append("\n");
            sb.append(formatTableRow("PC", sellPcVal, payPcVal)).append("\n");
            sb.append("-----------------------------------------------------\n");
            sb.append(formatTableRow("TOTAL", totalSellVal, totalPaymentVal)).append("\n");
            sb.append("-----------------------------------------------------\n");

            sb.append("*COM (").append(commRateVal.stripTrailingZeros().toPlainString()).append("%):-  ").append(fmtNum(commAmountVal)).append("*\n");
            sb.append("---------------------------------\n");
            sb.append("*TOTAL:-           ").append(fmtNum(totalAfterCommVal)).append("*\n");
            sb.append("*PAYMENT:-     ").append(fmtNum(totalPaymentVal)).append("*\n");
            sb.append("---------------------------------\n");

            BigDecimal todayNet = afterPayVal;
            if (pagarVal != null && pagarVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("                          *").append(fmtNum(afterPayVal)).append("*\n");
                sb.append("*PAGAR:-        ").append(fmtNum(pagarVal)).append("*\n");
                sb.append("---------------------------------\n");
                todayNet = afterPayVal.subtract(pagarVal);
            }

            netBalanceVal = todayNet.add(netOpening);
            appendBalanceSection(sb, todayNet, yeneVal, deneVal, netBalanceVal);
        }

        String message = sb.toString();

        String cleanMobile = customer.getMobileNumber().replaceAll("[^0-9]", "");
        if (cleanMobile.length() == 10) {
            cleanMobile = "91" + cleanMobile;
        }

        String encodedText = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        String whatsappUrl = "https://api.whatsapp.com/send?phone=" + cleanMobile + "&text=" + encodedText;

        return new WhatsAppStatementDTO(
                customer.getId(),
                customer.getName(),
                customer.getMobileNumber(),
                customer.getCity(),
                message,
                whatsappUrl
        );
    }

    private String formatCenteredCity(String rawCity) {
        String cityText = rawCity != null ? rawCity.trim().toUpperCase() : "";
        return String.format("           *%s*", cityText);
    }

    private String formatTableHeader() {
        return "                          *SELL*      *PAYMENT*";
    }

    private String formatTableRow(String label, BigDecimal sellVal, BigDecimal payVal) {
        String sellStr = fmtNum(sellVal);
        String payStr = fmtNum(payVal);
        if ("PO".equals(label)) {
            return String.format("*PO:-                  %6s     %6s*", sellStr, payStr);
        } else if ("PC".equals(label)) {
            return String.format("*PC:-                  %6s     %6s*", sellStr, payStr);
        } else {
            return String.format("*TOTAL:-           %6s     %6s*", sellStr, payStr);
        }
    }

    private void appendBalanceSection(StringBuilder sb, BigDecimal todayNet, BigDecimal yeneVal, BigDecimal deneVal, BigDecimal netBalanceVal) {
        if (yeneVal.compareTo(BigDecimal.ZERO) > 0 || deneVal.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("---------------------------------\n");
            if (todayNet.compareTo(BigDecimal.ZERO) >= 0) {
                sb.append("*TOTAL :-*        *").append(fmtNum(todayNet)).append(" yeṇe*\n");
            } else {
                sb.append("*TOTAL :-*        *").append(fmtNum(todayNet.abs())).append(" dene*\n");
            }

            sb.append("---------------------------------\n");
            if (yeneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("🔴 *MAGIL YENE:* *").append(fmtNum(yeneVal)).append("*\n");
            } else if (deneVal.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("🔴 *MAGIL DENE:* *").append(fmtNum(deneVal)).append("*\n");
            }
        }

        sb.append("---------------------------------\n");
        if (netBalanceVal.compareTo(BigDecimal.ZERO) >= 0) {
            sb.append("*TOTAL BALANCE DUE:* *").append(fmtNum(netBalanceVal)).append(" yeṇe*");
        } else {
            sb.append("*TOTAL BALANCE DUE:* *").append(fmtNum(netBalanceVal.abs())).append(" dene*");
        }
    }

    private String formatSingleRow(String label, BigDecimal val) {
        String valStr = fmtNum(val);
        int pad = 21;
        if ("MISS PAYMENT".equals(label)) {
            pad = 4;
        } else if ("PAYMENT".equals(label)) {
            pad = 15;
        } else if ("PAGAR".equals(label)) {
            pad = 21;
        } else if (label.startsWith("COM")) {
            pad = 14;
        } else if (label.endsWith("%")) {
            pad = 24;
        } else if ("TOTAL".equals(label)) {
            pad = 21;
        } else {
            pad = Math.max(2, 31 - label.length() - valStr.length());
        }
        return "*" + label + ":-" + String.format("%" + pad + "s", "") + valStr + "*";
    }

    private String fmtNum(BigDecimal val) {
        if (val == null) return "0";
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(val.abs());
    }

    public String getDefaultTemplate() {
        return "{centeredCity}\n\n" +
               "             SELL    PAYMENT\n" +
               "PO:-         {sellPo}     {paymentPo}\n" +
               "PC:-          {sellPc}      {paymentPc}\n" +
               "---------------------------------\n" +
               "TOTAL:-      {totalSell}     {totalPayment}\n\n" +
               "COMI*{commRate}%:-   {commissionAmount}\n" +
               "---------------------------------\n" +
               "Total:-      {totalAfterComm}\n" +
               "Payment:-    {totalPayment}\n" +
               "---------------------------------\n\n" +
               "Total yeṇe (Total येणे):- {netBalanceDue}\n\n" +
               "Pagar\n" +
               "40/60%";
    }
}
