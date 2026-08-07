package com.punabazar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger")
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalSell = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalPayment = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal previousBalance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal netBalanceDue = BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Ledger() {}

    public Ledger(Customer customer, LocalDate entryDate, BigDecimal totalSell, BigDecimal totalCommission, BigDecimal totalPayment, BigDecimal previousBalance, BigDecimal netBalanceDue) {
        this.customer = customer;
        this.entryDate = entryDate;
        this.totalSell = totalSell != null ? totalSell : BigDecimal.ZERO;
        this.totalCommission = totalCommission != null ? totalCommission : BigDecimal.ZERO;
        this.totalPayment = totalPayment != null ? totalPayment : BigDecimal.ZERO;
        this.previousBalance = previousBalance != null ? previousBalance : BigDecimal.ZERO;
        this.netBalanceDue = netBalanceDue != null ? netBalanceDue : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public BigDecimal getTotalSell() { return totalSell; }
    public void setTotalSell(BigDecimal totalSell) { this.totalSell = totalSell; }

    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public BigDecimal getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(BigDecimal previousBalance) { this.previousBalance = previousBalance; }

    public BigDecimal getNetBalanceDue() { return netBalanceDue; }
    public void setNetBalanceDue(BigDecimal netBalanceDue) { this.netBalanceDue = netBalanceDue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
