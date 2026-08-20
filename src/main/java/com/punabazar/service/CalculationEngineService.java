package com.punabazar.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculationEngineService {

    public BigDecimal calculateTotalSell(BigDecimal sellPo, Integer sellPc, BigDecimal rate) {
        if (sellPo == null) sellPo = BigDecimal.ZERO;
        if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
            return sellPo.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        return sellPo.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateCommission(BigDecimal totalSell, BigDecimal commissionPercentage) {
        if (totalSell == null) totalSell = BigDecimal.ZERO;
        if (commissionPercentage == null) commissionPercentage = new BigDecimal("10.00");

        return totalSell.multiply(commissionPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateCommission(BigDecimal totalSell, BigDecimal totalPayment, BigDecimal commissionPercentage) {
        return calculateCommission(totalSell, commissionPercentage);
    }

    public BigDecimal calculateNetBalanceDue(BigDecimal previousBalance, BigDecimal totalSell, BigDecimal commissionAmount, BigDecimal paymentAmount) {
        if (previousBalance == null) previousBalance = BigDecimal.ZERO;
        if (totalSell == null) totalSell = BigDecimal.ZERO;
        if (commissionAmount == null) commissionAmount = BigDecimal.ZERO;
        if (paymentAmount == null) paymentAmount = BigDecimal.ZERO;

        // Net Balance Due = Previous Balance + Total Sell - Commission Amount - Payment Received
        return previousBalance.add(totalSell)
                .subtract(commissionAmount)
                .subtract(paymentAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetBalanceDueWithShare(BigDecimal previousBalance,
                                                     BigDecimal totalSell,
                                                     BigDecimal commissionAmount,
                                                     BigDecimal paymentAmount,
                                                     BigDecimal pagarAmount,
                                                     BigDecimal farakAmount,
                                                     BigDecimal shareRate,
                                                     Boolean share30ProfitOnly) {
        if (previousBalance == null) previousBalance = BigDecimal.ZERO;
        if (totalSell == null) totalSell = BigDecimal.ZERO;
        if (commissionAmount == null) commissionAmount = BigDecimal.ZERO;
        if (paymentAmount == null) paymentAmount = BigDecimal.ZERO;
        if (pagarAmount == null) pagarAmount = BigDecimal.ZERO;
        if (farakAmount == null) farakAmount = BigDecimal.ZERO;

        BigDecimal totalAfterComm = totalSell.subtract(commissionAmount);
        BigDecimal afterPay = totalAfterComm.subtract(paymentAmount);
        BigDecimal runningNet = afterPay.subtract(pagarAmount).subtract(farakAmount);

        boolean is30ProfitOnly = Boolean.TRUE.equals(share30ProfitOnly);
        BigDecimal rateToApply = is30ProfitOnly ? new BigDecimal("30.00") : (shareRate != null ? shareRate : new BigDecimal("100.00"));
        boolean isShareEnabled = is30ProfitOnly || (shareRate != null && shareRate.compareTo(new BigDecimal("100.00")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0);

        BigDecimal shareAmount = BigDecimal.ZERO;
        if (isShareEnabled) {
            if (is30ProfitOnly) {
                if (runningNet.compareTo(BigDecimal.ZERO) > 0) {
                    shareAmount = runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            } else {
                shareAmount = runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal todayNet = runningNet.subtract(shareAmount);
        return previousBalance.add(todayNet).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTransactionCommission(com.punabazar.model.Customer customer, com.punabazar.model.Transaction tx) {
        if (tx == null) return BigDecimal.ZERO;
        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
        BigDecimal totalSell = sellPo.add(sellPc);

        if (customer != null && Boolean.TRUE.equals(customer.getCommissionEnabled())) {
            BigDecimal commRate = customer.getCommissionRate() != null ? customer.getCommissionRate() : new BigDecimal("10.00");
            return totalSell.multiply(commRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal calculateTransactionShare(com.punabazar.model.Customer customer, com.punabazar.model.Transaction tx) {
        if (tx == null) return BigDecimal.ZERO;
        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
        BigDecimal payPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;

        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        BigDecimal commAmount = calculateTransactionCommission(customer, tx);
        BigDecimal totalAfterComm = totalSell.subtract(commAmount);
        BigDecimal afterPay = totalAfterComm.subtract(totalPayment);

        BigDecimal pagarVal = (customer != null && Boolean.TRUE.equals(customer.getPagarEnabled())) ? (customer.getPagar() != null ? customer.getPagar() : BigDecimal.ZERO) : BigDecimal.ZERO;
        BigDecimal farakVal = tx.getFarak() != null ? tx.getFarak() : ((customer != null && customer.getFarak() != null) ? customer.getFarak() : BigDecimal.ZERO);

        BigDecimal runningNet = afterPay.subtract(pagarVal).subtract(farakVal);

        boolean is30ProfitOnly = customer != null && Boolean.TRUE.equals(customer.getShare30ProfitOnly());
        BigDecimal shareRate = customer != null ? customer.getShareRate() : new BigDecimal("100.00");
        boolean isShareEnabled = is30ProfitOnly || (shareRate != null && shareRate.compareTo(new BigDecimal("100.00")) < 0 && shareRate.compareTo(BigDecimal.ZERO) > 0);

        if (isShareEnabled) {
            BigDecimal rateToApply = is30ProfitOnly ? new BigDecimal("30.00") : (shareRate != null ? shareRate : new BigDecimal("100.00"));
            if (is30ProfitOnly) {
                if (runningNet.compareTo(BigDecimal.ZERO) > 0) {
                    return runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            } else {
                return runningNet.multiply(rateToApply).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal calculateTransactionTodayNet(com.punabazar.model.Customer customer, com.punabazar.model.Transaction tx) {
        if (tx == null) return BigDecimal.ZERO;
        BigDecimal sellPo = tx.getSellPo() != null ? tx.getSellPo() : BigDecimal.ZERO;
        BigDecimal sellPc = tx.getSellPcAmount() != null ? tx.getSellPcAmount() : BigDecimal.ZERO;
        BigDecimal payPo = tx.getPaymentPo() != null ? tx.getPaymentPo() : BigDecimal.ZERO;
        BigDecimal payPc = tx.getPaymentPc() != null ? tx.getPaymentPc() : BigDecimal.ZERO;

        BigDecimal totalSell = sellPo.add(sellPc);
        BigDecimal totalPayment = payPo.add(payPc);

        BigDecimal commAmount = calculateTransactionCommission(customer, tx);
        BigDecimal totalAfterComm = totalSell.subtract(commAmount);
        BigDecimal afterPay = totalAfterComm.subtract(totalPayment);

        BigDecimal pagarVal = (customer != null && Boolean.TRUE.equals(customer.getPagarEnabled())) ? (customer.getPagar() != null ? customer.getPagar() : BigDecimal.ZERO) : BigDecimal.ZERO;
        BigDecimal farakVal = tx.getFarak() != null ? tx.getFarak() : ((customer != null && customer.getFarak() != null) ? customer.getFarak() : BigDecimal.ZERO);

        BigDecimal runningNet = afterPay.subtract(pagarVal).subtract(farakVal);
        BigDecimal shareAmount = calculateTransactionShare(customer, tx);

        return runningNet.subtract(shareAmount).setScale(2, RoundingMode.HALF_UP);
    }
}
