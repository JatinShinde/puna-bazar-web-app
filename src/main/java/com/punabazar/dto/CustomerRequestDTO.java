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
    private Boolean pagarEnabled;
    private Boolean commissionEnabled;
    private Boolean share30ProfitOnly;
    private BigDecimal share30ProfitOnlyRate;
    private Boolean weeklyCommissionEnabled;
    private BigDecimal weeklyCommissionRate;
    private Boolean weeklyPagarEnabled;
    private BigDecimal weeklyPagar;
    private Boolean weeklyShareEnabled;
    private BigDecimal weeklyShareRate;
    private Boolean weeklyShare30ProfitOnly;
    private BigDecimal weeklyShare30ProfitOnlyRate;

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

    public Boolean getPagarEnabled() { return pagarEnabled; }
    public void setPagarEnabled(Boolean pagarEnabled) { this.pagarEnabled = pagarEnabled; }

    public Boolean getCommissionEnabled() { return commissionEnabled; }
    public Boolean isCommissionEnabled() { return commissionEnabled; }
    public void setCommissionEnabled(Boolean commissionEnabled) { this.commissionEnabled = commissionEnabled; }

    public Boolean getShare30ProfitOnly() { return share30ProfitOnly; }
    public void setShare30ProfitOnly(Boolean share30ProfitOnly) { this.share30ProfitOnly = share30ProfitOnly; }

    public BigDecimal getShare30ProfitOnlyRate() { return share30ProfitOnlyRate; }
    public void setShare30ProfitOnlyRate(BigDecimal share30ProfitOnlyRate) { this.share30ProfitOnlyRate = share30ProfitOnlyRate; }

    public Boolean getWeeklyCommissionEnabled() { return weeklyCommissionEnabled; }
    public void setWeeklyCommissionEnabled(Boolean weeklyCommissionEnabled) { this.weeklyCommissionEnabled = weeklyCommissionEnabled; }

    public BigDecimal getWeeklyCommissionRate() { return weeklyCommissionRate; }
    public void setWeeklyCommissionRate(BigDecimal weeklyCommissionRate) { this.weeklyCommissionRate = weeklyCommissionRate; }

    public Boolean getWeeklyPagarEnabled() { return weeklyPagarEnabled; }
    public void setWeeklyPagarEnabled(Boolean weeklyPagarEnabled) { this.weeklyPagarEnabled = weeklyPagarEnabled; }

    public BigDecimal getWeeklyPagar() { return weeklyPagar; }
    public void setWeeklyPagar(BigDecimal weeklyPagar) { this.weeklyPagar = weeklyPagar; }

    public Boolean getWeeklyShareEnabled() { return weeklyShareEnabled; }
    public void setWeeklyShareEnabled(Boolean weeklyShareEnabled) { this.weeklyShareEnabled = weeklyShareEnabled; }

    public BigDecimal getWeeklyShareRate() { return weeklyShareRate; }
    public void setWeeklyShareRate(BigDecimal weeklyShareRate) { this.weeklyShareRate = weeklyShareRate; }

    public Boolean getWeeklyShare30ProfitOnly() { return weeklyShare30ProfitOnly; }
    public void setWeeklyShare30ProfitOnly(Boolean weeklyShare30ProfitOnly) { this.weeklyShare30ProfitOnly = weeklyShare30ProfitOnly; }

    public BigDecimal getWeeklyShare30ProfitOnlyRate() { return weeklyShare30ProfitOnlyRate; }
    public void setWeeklyShare30ProfitOnlyRate(BigDecimal weeklyShare30ProfitOnlyRate) { this.weeklyShare30ProfitOnlyRate = weeklyShare30ProfitOnlyRate; }
}
