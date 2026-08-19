package com.punabazar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = true, length = 15)
    private String mobileNumber;

    @Column(nullable = true, length = 50)
    private String city;

    private String marketZone;

    @Column(precision = 12, scale = 2)
    private BigDecimal previousBalance = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("10.00");

    private Boolean commissionEnabled = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal pagar = BigDecimal.ZERO;

    private Boolean pagarEnabled = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal magilBaki = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal yene = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal dene = BigDecimal.ZERO;

    @Column(length = 10)
    private String balanceType = "YENE"; // YENE or DENE

    @Column(precision = 12, scale = 2)
    private BigDecimal farak = BigDecimal.ZERO;

    @Column(length = 255)
    private String marketCodes = "PO,PC";

    @Column(length = 30)
    private String receiptStyle = "STANDARD"; // STANDARD, SHARE_PERCENT, FARAK_SHARE, SIMPLE

    @Column(precision = 5, scale = 2)
    private BigDecimal shareRate = new BigDecimal("100.00");

    private Boolean share30ProfitOnly = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Customer() {}

    public Customer(String name, String mobileNumber, String city, String marketZone, BigDecimal previousBalance) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.city = city;
        this.marketZone = marketZone;
        this.previousBalance = previousBalance != null ? previousBalance : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMarketZone() { return marketZone; }
    public void setMarketZone(String marketZone) { this.marketZone = marketZone; }

    public BigDecimal getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(BigDecimal previousBalance) { this.previousBalance = previousBalance; }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }

    public Boolean getCommissionEnabled() { return commissionEnabled != null ? commissionEnabled : false; }
    public Boolean isCommissionEnabled() { return getCommissionEnabled(); }
    public void setCommissionEnabled(Boolean commissionEnabled) { this.commissionEnabled = commissionEnabled; }

    public BigDecimal getPagar() { return pagar; }
    public void setPagar(BigDecimal pagar) { this.pagar = pagar; }

    public Boolean getPagarEnabled() { return pagarEnabled != null ? pagarEnabled : false; }
    public void setPagarEnabled(Boolean pagarEnabled) { this.pagarEnabled = pagarEnabled; }

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

    public Boolean getShare30ProfitOnly() { return share30ProfitOnly != null ? share30ProfitOnly : false; }
    public void setShare30ProfitOnly(Boolean share30ProfitOnly) { this.share30ProfitOnly = share30ProfitOnly; }

    @Transient
    private BigDecimal todayNet = BigDecimal.ZERO;

    public BigDecimal getTodayNet() { return todayNet; }
    public void setTodayNet(BigDecimal todayNet) { this.todayNet = todayNet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
