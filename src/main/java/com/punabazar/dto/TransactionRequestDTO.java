package com.punabazar.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequestDTO {
    private Long customerId;
    private LocalDate transactionDate;
    private BigDecimal sellPo;
    private BigDecimal sellPc;
    private BigDecimal paymentPo;
    private BigDecimal paymentPc;
    private BigDecimal magilBaki;
    private BigDecimal pagarAmount;
    private String pagarNote;
    private BigDecimal rate;
    private BigDecimal commissionPercentage;
    private BigDecimal paymentAmount;
    private String paymentMode;
    private String notes;
    private String receiptStyle;
    private BigDecimal shareRate;
    private BigDecimal farak;

    public TransactionRequestDTO() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getSellPo() { return sellPo; }
    public void setSellPo(BigDecimal sellPo) { this.sellPo = sellPo; }

    public BigDecimal getSellPc() { return sellPc; }
    public void setSellPc(BigDecimal sellPc) { this.sellPc = sellPc; }

    public BigDecimal getPaymentPo() { return paymentPo; }
    public void setPaymentPo(BigDecimal paymentPo) { this.paymentPo = paymentPo; }

    public BigDecimal getPaymentPc() { return paymentPc; }
    public void setPaymentPc(BigDecimal paymentPc) { this.paymentPc = paymentPc; }

    public BigDecimal getMagilBaki() { return magilBaki; }
    public void setMagilBaki(BigDecimal magilBaki) { this.magilBaki = magilBaki; }

    public BigDecimal getPagarAmount() { return pagarAmount; }
    public void setPagarAmount(BigDecimal pagarAmount) { this.pagarAmount = pagarAmount; }

    public String getPagarNote() { return pagarNote; }
    public void setPagarNote(String pagarNote) { this.pagarNote = pagarNote; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(BigDecimal commissionPercentage) { this.commissionPercentage = commissionPercentage; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReceiptStyle() { return receiptStyle; }
    public void setReceiptStyle(String receiptStyle) { this.receiptStyle = receiptStyle; }

    public BigDecimal getShareRate() { return shareRate; }
    public void setShareRate(BigDecimal shareRate) { this.shareRate = shareRate; }

    public BigDecimal getFarak() { return farak; }
    public void setFarak(BigDecimal farak) { this.farak = farak; }
}
