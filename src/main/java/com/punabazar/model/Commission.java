package com.punabazar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission")
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ratePercentage = new BigDecimal("10.00");

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Commission() {}

    public Commission(Transaction transaction, BigDecimal ratePercentage, BigDecimal commissionAmount) {
        this.transaction = transaction;
        this.ratePercentage = ratePercentage != null ? ratePercentage : new BigDecimal("10.00");
        this.commissionAmount = commissionAmount != null ? commissionAmount : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public BigDecimal getRatePercentage() { return ratePercentage; }
    public void setRatePercentage(BigDecimal ratePercentage) { this.ratePercentage = ratePercentage; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
