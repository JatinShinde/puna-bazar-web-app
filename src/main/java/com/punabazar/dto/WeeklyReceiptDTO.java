package com.punabazar.dto;

import java.math.BigDecimal;

public class WeeklyReceiptDTO {
    private Long customerId;
    private String customerName;
    private String city;
    private String marketZone;
    private BigDecimal mondayNet;
    private BigDecimal tuesdayNet;
    private BigDecimal wednesdayNet;
    private BigDecimal thursdayNet;
    private BigDecimal fridayNet;
    private BigDecimal saturdayNet;
    private BigDecimal sundayNet;
    private BigDecimal weeklyTotalNet;
    private String weeklyTotalStatus; // YENE or DENE
    private String formattedWeeklyMessage;

    public WeeklyReceiptDTO() {}

    public WeeklyReceiptDTO(Long customerId, String customerName, String city, String marketZone,
                            BigDecimal mondayNet, BigDecimal tuesdayNet, BigDecimal wednesdayNet,
                            BigDecimal thursdayNet, BigDecimal fridayNet, BigDecimal saturdayNet,
                            BigDecimal sundayNet, BigDecimal weeklyTotalNet, String weeklyTotalStatus,
                            String formattedWeeklyMessage) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.city = city;
        this.marketZone = marketZone;
        this.mondayNet = mondayNet != null ? mondayNet : BigDecimal.ZERO;
        this.tuesdayNet = tuesdayNet != null ? tuesdayNet : BigDecimal.ZERO;
        this.wednesdayNet = wednesdayNet != null ? wednesdayNet : BigDecimal.ZERO;
        this.thursdayNet = thursdayNet != null ? thursdayNet : BigDecimal.ZERO;
        this.fridayNet = fridayNet != null ? fridayNet : BigDecimal.ZERO;
        this.saturdayNet = saturdayNet != null ? saturdayNet : BigDecimal.ZERO;
        this.sundayNet = sundayNet != null ? sundayNet : BigDecimal.ZERO;
        this.weeklyTotalNet = weeklyTotalNet != null ? weeklyTotalNet : BigDecimal.ZERO;
        this.weeklyTotalStatus = weeklyTotalStatus != null ? weeklyTotalStatus : "YENE";
        this.formattedWeeklyMessage = formattedWeeklyMessage;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMarketZone() { return marketZone; }
    public void setMarketZone(String marketZone) { this.marketZone = marketZone; }

    public BigDecimal getMondayNet() { return mondayNet; }
    public void setMondayNet(BigDecimal mondayNet) { this.mondayNet = mondayNet; }

    public BigDecimal getTuesdayNet() { return tuesdayNet; }
    public void setTuesdayNet(BigDecimal tuesdayNet) { this.tuesdayNet = tuesdayNet; }

    public BigDecimal getWednesdayNet() { return wednesdayNet; }
    public void setWednesdayNet(BigDecimal wednesdayNet) { this.wednesdayNet = wednesdayNet; }

    public BigDecimal getThursdayNet() { return thursdayNet; }
    public void setThursdayNet(BigDecimal thursdayNet) { this.thursdayNet = thursdayNet; }

    public BigDecimal getFridayNet() { return fridayNet; }
    public void setFridayNet(BigDecimal fridayNet) { this.fridayNet = fridayNet; }

    public BigDecimal getSaturdayNet() { return saturdayNet; }
    public void setSaturdayNet(BigDecimal saturdayNet) { this.saturdayNet = saturdayNet; }

    public BigDecimal getSundayNet() { return sundayNet; }
    public void setSundayNet(BigDecimal sundayNet) { this.sundayNet = sundayNet; }

    public BigDecimal getWeeklyTotalNet() { return weeklyTotalNet; }
    public void setWeeklyTotalNet(BigDecimal weeklyTotalNet) { this.weeklyTotalNet = weeklyTotalNet; }

    public String getWeeklyTotalStatus() { return weeklyTotalStatus; }
    public void setWeeklyTotalStatus(String weeklyTotalStatus) { this.weeklyTotalStatus = weeklyTotalStatus; }

    public String getFormattedWeeklyMessage() { return formattedWeeklyMessage; }
    public void setFormattedWeeklyMessage(String formattedWeeklyMessage) { this.formattedWeeklyMessage = formattedWeeklyMessage; }
}
