package com.arindustries.repository;

import com.arindustries.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);
    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customer.id = :customerId AND p.reversed = false")
    BigDecimal sumByCustomerId(@Param("customerId") Long customerId);
}
