package com.punabazar.controller;

import com.punabazar.dto.CustomerRequestDTO;
import com.punabazar.model.Customer;
import com.punabazar.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getAll(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false)
                                 @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
                                 java.time.LocalDate date) {
        if (search != null && !search.trim().isEmpty()) {
            return customerService.searchCustomers(search, date);
        } else if (city != null && !city.trim().isEmpty()) {
            return customerService.getCustomersByCity(city, date);
        }
        return customerService.getAllCustomers(date);
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping("/markets")
    public List<String> getMarkets() {
        return customerService.getDistinctMarkets();
    }

    @PostMapping
    public Customer create(@RequestBody CustomerRequestDTO request) {
        return customerService.createCustomerWithTrade(request);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody CustomerRequestDTO request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
