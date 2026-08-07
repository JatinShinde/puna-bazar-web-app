package com.punabazar.repository;

import com.punabazar.model.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface CommissionRepository extends JpaRepository<Commission, Long> {
    
    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.transaction.transactionDate = :date")
    Double getTodayTotalCommission(@Param("date") LocalDate date);

    @Modifying
    @Query("DELETE FROM Commission c WHERE c.transaction.customer.id = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
