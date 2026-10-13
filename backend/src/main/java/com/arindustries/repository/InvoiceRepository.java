package com.arindustries.repository;

import com.arindustries.entity.Invoice;
import com.arindustries.entity.InvoiceSequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:search IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(i.customer.customerName) LIKE LOWER(CONCAT('%',:search,'%'))) " +
           "AND (:status IS NULL OR i.invoiceStatus = :status) " +
           "AND (:paymentStatus IS NULL OR i.paymentStatus = :paymentStatus) " +
           "AND (:fromDate IS NULL OR i.invoiceDate >= :fromDate) " +
           "AND (:toDate IS NULL OR i.invoiceDate <= :toDate)")
    Page<Invoice> findWithFilters(@Param("search") String search,
                                   @Param("status") com.arindustries.enums.InvoiceStatus status,
                                   @Param("paymentStatus") com.arindustries.enums.PaymentStatus paymentStatus,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate,
                                   Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.invoiceDate >= :from AND i.invoiceDate <= :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.paymentStatus = com.arindustries.enums.PaymentStatus.PENDING OR i.paymentStatus = com.arindustries.enums.PaymentStatus.PARTIAL")
    long countPendingInvoices();

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT i FROM Invoice i WHERE i.customer.id = :customerId ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}
