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
        return calculateCommission(totalSell, BigDecimal.ZERO, commissionPercentage);
    }

    public BigDecimal calculateCommission(BigDecimal totalSell, BigDecimal totalPayment, BigDecimal commissionPercentage) {
        if (totalSell == null) totalSell = BigDecimal.ZERO;
        if (totalPayment == null) totalPayment = BigDecimal.ZERO;
        if (commissionPercentage == null) commissionPercentage = new BigDecimal("10.00");

        // Commission is ONLY calculated when there is profit (totalSell > totalPayment / YENE)
        if (totalSell.compareTo(totalPayment) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return totalSell.multiply(commissionPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
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
}
