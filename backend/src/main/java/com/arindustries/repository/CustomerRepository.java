package com.arindustries.repository;

import com.arindustries.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String code);
    Optional<Customer> findByCustomerName(String name);
    boolean existsByGstin(String gstin);
    boolean existsByCustomerCode(String code);

    @Query("SELECT c FROM Customer c WHERE c.active = :active AND " +
           "(:search IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.gstin) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findByActiveAndSearch(@Param("active") boolean active, @Param("search") String search, Pageable pageable);

    Page<Customer> findByActive(boolean active, Pageable pageable);
}
