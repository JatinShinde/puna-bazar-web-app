package com.punabazar.repository;

import com.punabazar.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);
    List<Payment> findByPaymentDate(LocalDate date);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate = :date")
    Double getTodayTotalPayments(@Param("date") LocalDate date);

    void deleteByCustomerId(Long customerId);
}
