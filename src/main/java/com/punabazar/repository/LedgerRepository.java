package com.punabazar.repository;

import com.punabazar.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    List<Ledger> findByCustomerIdOrderByEntryDateDesc(Long customerId);
    List<Ledger> findByEntryDate(LocalDate entryDate);

    void deleteByCustomerId(Long customerId);
}
