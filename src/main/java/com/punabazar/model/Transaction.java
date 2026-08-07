package com.punabazar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal sellPo = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal sellPcAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal paymentPo = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal paymentPc = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal magilBaki = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal pagarAmount = BigDecimal.ZERO;

    private Integer sellPc = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalSell = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Transaction() {}

    public Transaction(Customer customer, LocalDate transactionDate, BigDecimal sellPo, BigDecimal sellPcAmount, BigDecimal paymentPo, BigDecimal paymentPc, BigDecimal totalSell) {
        this.customer = customer;
        this.transactionDate = transactionDate;
        this.sellPo = sellPo != null ? sellPo : BigDecimal.ZERO;
        this.sellPcAmount = sellPcAmount != null ? sellPcAmount : BigDecimal.ZERO;
        this.paymentPo = paymentPo != null ? paymentPo : BigDecimal.ZERO;
        this.paymentPc = paymentPc != null ? paymentPc : BigDecimal.ZERO;
        this.totalSell = totalSell != null ? totalSell : BigDecimal.ZERO;
    }

    public Transaction(Customer customer, LocalDate transactionDate, BigDecimal sellPo, BigDecimal sellPcAmount, BigDecimal paymentPo, BigDecimal paymentPc, BigDecimal magilBaki, BigDecimal pagarAmount, BigDecimal totalSell) {
        this.customer = customer;
        this.transactionDate = transactionDate;
        this.sellPo = sellPo != null ? sellPo : BigDecimal.ZERO;
        this.sellPcAmount = sellPcAmount != null ? sellPcAmount : BigDecimal.ZERO;
        this.paymentPo = paymentPo != null ? paymentPo : BigDecimal.ZERO;
        this.paymentPc = paymentPc != null ? paymentPc : BigDecimal.ZERO;
        this.magilBaki = magilBaki != null ? magilBaki : BigDecimal.ZERO;
        this.pagarAmount = pagarAmount != null ? pagarAmount : BigDecimal.ZERO;
        this.totalSell = totalSell != null ? totalSell : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getSellPo() { return sellPo; }
    public void setSellPo(BigDecimal sellPo) { this.sellPo = sellPo; }

    public BigDecimal getSellPcAmount() { return sellPcAmount; }
    public void setSellPcAmount(BigDecimal sellPcAmount) { this.sellPcAmount = sellPcAmount; }

    public BigDecimal getPaymentPo() { return paymentPo; }
    public void setPaymentPo(BigDecimal paymentPo) { this.paymentPo = paymentPo; }

    public BigDecimal getPaymentPc() { return paymentPc; }
    public void setPaymentPc(BigDecimal paymentPc) { this.paymentPc = paymentPc; }

    public BigDecimal getMagilBaki() { return magilBaki; }
    public void setMagilBaki(BigDecimal magilBaki) { this.magilBaki = magilBaki; }

    public BigDecimal getPagarAmount() { return pagarAmount; }
    public void setPagarAmount(BigDecimal pagarAmount) { this.pagarAmount = pagarAmount; }

    public Integer getSellPc() { return sellPc; }
    public void setSellPc(Integer sellPc) { this.sellPc = sellPc; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getTotalSell() { return totalSell; }
    public void setTotalSell(BigDecimal totalSell) { this.totalSell = totalSell; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
