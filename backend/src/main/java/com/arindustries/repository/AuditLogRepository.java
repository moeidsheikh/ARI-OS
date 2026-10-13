package com.arindustries.repository;

import com.arindustries.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:search IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(a.actor) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<AuditLog> findWithFilters(@Param("module") String module, @Param("search") String search, Pageable pageable);
}
