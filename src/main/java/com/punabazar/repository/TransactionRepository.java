package com.punabazar.repository;

import com.punabazar.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerIdOrderByTransactionDateDesc(Long customerId);
    List<Transaction> findByTransactionDate(LocalDate date);
    boolean existsByCustomerIdAndTransactionDate(Long customerId, LocalDate transactionDate);
    List<Transaction> findByCustomerIdAndTransactionDate(Long customerId, LocalDate transactionDate);
    java.util.Optional<Transaction> findTopByCustomerIdAndTransactionDateOrderByCreatedAtDesc(Long customerId, LocalDate transactionDate);

    @Query("SELECT SUM(t.totalSell) FROM Transaction t WHERE t.transactionDate = :date")
    Double getTodayTotalSell(@Param("date") LocalDate date);

    @Query("SELECT SUM(t.sellPo) FROM Transaction t WHERE t.transactionDate = :date")
    Double getTodayPoSell(@Param("date") LocalDate date);

    @Query("SELECT SUM(t.sellPcAmount) FROM Transaction t WHERE t.transactionDate = :date")
    Double getTodayPcSell(@Param("date") LocalDate date);

    void deleteByCustomerId(Long customerId);
}
