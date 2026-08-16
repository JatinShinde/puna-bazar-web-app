package com.punabazar.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardMetricsDTO {
    private BigDecimal todayTotalSell;
    private BigDecimal todayPoSell;
    private BigDecimal todayPcSell;
    private BigDecimal todayTotalPayment;
    private BigDecimal todayTotalCommission;
    private BigDecimal todayProfitLoss;
    private String todayProfitLossStatus;
    private BigDecimal totalCustomerBalance;
    private Long activeCustomerCount;
    private List<String> activeMarkets;
    private Long generatedReceiptCount;
    private Long totalCustomerCount;
    private List<String> generatedReceiptMarkets;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(BigDecimal todayTotalSell, BigDecimal todayPoSell, BigDecimal todayPcSell, BigDecimal todayTotalPayment, BigDecimal todayTotalCommission, BigDecimal todayProfitLoss, String todayProfitLossStatus, BigDecimal totalCustomerBalance, Long activeCustomerCount, List<String> activeMarkets) {
        this(todayTotalSell, todayPoSell, todayPcSell, todayTotalPayment, todayTotalCommission, todayProfitLoss, todayProfitLossStatus, totalCustomerBalance, activeCustomerCount, activeMarkets, 0L, activeCustomerCount, new java.util.ArrayList<>());
    }

    public DashboardMetricsDTO(BigDecimal todayTotalSell, BigDecimal todayPoSell, BigDecimal todayPcSell, BigDecimal todayTotalPayment, BigDecimal todayTotalCommission, BigDecimal todayProfitLoss, String todayProfitLossStatus, BigDecimal totalCustomerBalance, Long activeCustomerCount, List<String> activeMarkets, Long generatedReceiptCount, Long totalCustomerCount) {
        this(todayTotalSell, todayPoSell, todayPcSell, todayTotalPayment, todayTotalCommission, todayProfitLoss, todayProfitLossStatus, totalCustomerBalance, activeCustomerCount, activeMarkets, generatedReceiptCount, totalCustomerCount, new java.util.ArrayList<>());
    }

    public DashboardMetricsDTO(BigDecimal todayTotalSell, BigDecimal todayPoSell, BigDecimal todayPcSell, BigDecimal todayTotalPayment, BigDecimal todayTotalCommission, BigDecimal todayProfitLoss, String todayProfitLossStatus, BigDecimal totalCustomerBalance, Long activeCustomerCount, List<String> activeMarkets, Long generatedReceiptCount, Long totalCustomerCount, List<String> generatedReceiptMarkets) {
        this.todayTotalSell = todayTotalSell;
        this.todayPoSell = todayPoSell;
        this.todayPcSell = todayPcSell;
        this.todayTotalPayment = todayTotalPayment;
        this.todayTotalCommission = todayTotalCommission;
        this.todayProfitLoss = todayProfitLoss;
        this.todayProfitLossStatus = todayProfitLossStatus;
        this.totalCustomerBalance = totalCustomerBalance;
        this.activeCustomerCount = activeCustomerCount;
        this.activeMarkets = activeMarkets;
        this.generatedReceiptCount = generatedReceiptCount;
        this.totalCustomerCount = totalCustomerCount;
        this.generatedReceiptMarkets = generatedReceiptMarkets;
    }

    public DashboardMetricsDTO(BigDecimal todayTotalSell, BigDecimal todayTotalPayment, BigDecimal todayTotalCommission, BigDecimal totalCustomerBalance, Long activeCustomerCount, List<String> activeMarkets) {
        this(todayTotalSell, BigDecimal.ZERO, BigDecimal.ZERO, todayTotalPayment, todayTotalCommission, BigDecimal.ZERO, "PROFIT", totalCustomerBalance, activeCustomerCount, activeMarkets, 0L, activeCustomerCount);
    }

    public BigDecimal getTodayTotalSell() { return todayTotalSell; }
    public void setTodayTotalSell(BigDecimal todayTotalSell) { this.todayTotalSell = todayTotalSell; }

    public BigDecimal getTodayPoSell() { return todayPoSell; }
    public void setTodayPoSell(BigDecimal todayPoSell) { this.todayPoSell = todayPoSell; }

    public BigDecimal getTodayPcSell() { return todayPcSell; }
    public void setTodayPcSell(BigDecimal todayPcSell) { this.todayPcSell = todayPcSell; }

    public BigDecimal getTodayTotalPayment() { return todayTotalPayment; }
    public void setTodayTotalPayment(BigDecimal todayTotalPayment) { this.todayTotalPayment = todayTotalPayment; }

    public BigDecimal getTodayTotalCommission() { return todayTotalCommission; }
    public void setTodayTotalCommission(BigDecimal todayTotalCommission) { this.todayTotalCommission = todayTotalCommission; }

    public BigDecimal getTodayProfitLoss() { return todayProfitLoss; }
    public void setTodayProfitLoss(BigDecimal todayProfitLoss) { this.todayProfitLoss = todayProfitLoss; }

    public String getTodayProfitLossStatus() { return todayProfitLossStatus; }
    public void setTodayProfitLossStatus(String todayProfitLossStatus) { this.todayProfitLossStatus = todayProfitLossStatus; }

    public BigDecimal getTotalCustomerBalance() { return totalCustomerBalance; }
    public void setTotalCustomerBalance(BigDecimal totalCustomerBalance) { this.totalCustomerBalance = totalCustomerBalance; }

    public Long getActiveCustomerCount() { return activeCustomerCount; }
    public void setActiveCustomerCount(Long activeCustomerCount) { this.activeCustomerCount = activeCustomerCount; }

    public List<String> getActiveMarkets() { return activeMarkets; }
    public void setActiveMarkets(List<String> activeMarkets) { this.activeMarkets = activeMarkets; }

    public Long getGeneratedReceiptCount() { return generatedReceiptCount; }
    public void setGeneratedReceiptCount(Long generatedReceiptCount) { this.generatedReceiptCount = generatedReceiptCount; }

    public Long getTotalCustomerCount() { return totalCustomerCount; }
    public void setTotalCustomerCount(Long totalCustomerCount) { this.totalCustomerCount = totalCustomerCount; }

    public List<String> getGeneratedReceiptMarkets() { return generatedReceiptMarkets; }
    public void setGeneratedReceiptMarkets(List<String> generatedReceiptMarkets) { this.generatedReceiptMarkets = generatedReceiptMarkets; }
}
