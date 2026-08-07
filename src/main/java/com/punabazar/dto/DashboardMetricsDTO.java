package com.punabazar.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardMetricsDTO {
    private BigDecimal todayTotalSell;
    private BigDecimal todayTotalPayment;
    private BigDecimal todayTotalCommission;
    private BigDecimal totalCustomerBalance;
    private Long activeCustomerCount;
    private List<String> activeMarkets;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(BigDecimal todayTotalSell, BigDecimal todayTotalPayment, BigDecimal todayTotalCommission, BigDecimal totalCustomerBalance, Long activeCustomerCount, List<String> activeMarkets) {
        this.todayTotalSell = todayTotalSell;
        this.todayTotalPayment = todayTotalPayment;
        this.todayTotalCommission = todayTotalCommission;
        this.totalCustomerBalance = totalCustomerBalance;
        this.activeCustomerCount = activeCustomerCount;
        this.activeMarkets = activeMarkets;
    }

    public BigDecimal getTodayTotalSell() { return todayTotalSell; }
    public void setTodayTotalSell(BigDecimal todayTotalSell) { this.todayTotalSell = todayTotalSell; }

    public BigDecimal getTodayTotalPayment() { return todayTotalPayment; }
    public void setTodayTotalPayment(BigDecimal todayTotalPayment) { this.todayTotalPayment = todayTotalPayment; }

    public BigDecimal getTodayTotalCommission() { return todayTotalCommission; }
    public void setTodayTotalCommission(BigDecimal todayTotalCommission) { this.todayTotalCommission = todayTotalCommission; }

    public BigDecimal getTotalCustomerBalance() { return totalCustomerBalance; }
    public void setTotalCustomerBalance(BigDecimal totalCustomerBalance) { this.totalCustomerBalance = totalCustomerBalance; }

    public Long getActiveCustomerCount() { return activeCustomerCount; }
    public void setActiveCustomerCount(Long activeCustomerCount) { this.activeCustomerCount = activeCustomerCount; }

    public List<String> getActiveMarkets() { return activeMarkets; }
    public void setActiveMarkets(List<String> activeMarkets) { this.activeMarkets = activeMarkets; }
}
