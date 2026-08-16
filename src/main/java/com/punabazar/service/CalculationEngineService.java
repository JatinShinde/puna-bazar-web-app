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
}
