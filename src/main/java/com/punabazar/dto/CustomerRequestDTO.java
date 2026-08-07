package com.punabazar.dto;

import java.math.BigDecimal;

public class CustomerRequestDTO {
    private String name;
    private String mobileNumber;
    private String city;
    private String marketZone;
    private BigDecimal po; // Puna Open (Previous/Opening Balance)
    private Integer pc;    // Puna Close (Quantity Count)
    private BigDecimal sell; // Sell Amount
    private BigDecimal commissionPercentage; // Commission %
    private BigDecimal payment; // Payment Received
    private BigDecimal pagar;   // Calculated Pagar / Net Balance
    private BigDecimal magilBaki; // Magil Baki
    private BigDecimal yene;     // Yene (Receivable Opening Balance)
    private BigDecimal dene;     // Dene (Payable Opening Balance)
    private String balanceType;  // YENE or DENE
    private BigDecimal farak;    // Farak / Adjustment
    private String marketCodes;  // e.g. "PO,PC,SO,SC"
    private String receiptStyle; // STANDARD, SHARE_PERCENT, FARAK_SHARE, SIMPLE
    private BigDecimal shareRate; // e.g. 40.00%

    public CustomerRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMarketZone() { return marketZone; }
    public void setMarketZone(String marketZone) { this.marketZone = marketZone; }

    public BigDecimal getPo() { return po; }
    public void setPo(BigDecimal po) { this.po = po; }

    public Integer getPc() { return pc; }
    public void setPc(Integer pc) { this.pc = pc; }

    public BigDecimal getSell() { return sell; }
    public void setSell(BigDecimal sell) { this.sell = sell; }

    public BigDecimal getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(BigDecimal commissionPercentage) { this.commissionPercentage = commissionPercentage; }

    public BigDecimal getPayment() { return payment; }
    public void setPayment(BigDecimal payment) { this.payment = payment; }

    public BigDecimal getPagar() { return pagar; }
    public void setPagar(BigDecimal pagar) { this.pagar = pagar; }

    public BigDecimal getMagilBaki() { return magilBaki; }
    public void setMagilBaki(BigDecimal magilBaki) { this.magilBaki = magilBaki; }

    public BigDecimal getYene() { return yene; }
    public void setYene(BigDecimal yene) { this.yene = yene; }

    public BigDecimal getDene() { return dene; }
    public void setDene(BigDecimal dene) { this.dene = dene; }

    public String getBalanceType() { return balanceType; }
    public void setBalanceType(String balanceType) { this.balanceType = balanceType; }

    public BigDecimal getFarak() { return farak; }
    public void setFarak(BigDecimal farak) { this.farak = farak; }

    public String getMarketCodes() { return marketCodes; }
    public void setMarketCodes(String marketCodes) { this.marketCodes = marketCodes; }

    public String getReceiptStyle() { return receiptStyle; }
    public void setReceiptStyle(String receiptStyle) { this.receiptStyle = receiptStyle; }

    public BigDecimal getShareRate() { return shareRate; }
    public void setShareRate(BigDecimal shareRate) { this.shareRate = shareRate; }
}
