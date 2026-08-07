package com.punabazar.repository;

import com.punabazar.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    List<Customer> findByCityIgnoreCase(String city);
    
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.city) LIKE LOWER(CONCAT('%', :query, '%')) OR c.mobileNumber LIKE CONCAT('%', :query, '%')")
    List<Customer> searchCustomers(@Param("query") String query);

    @Query("SELECT DISTINCT c.city FROM Customer c ORDER BY c.city ASC")
    List<String> findDistinctCities();
}
