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
}
