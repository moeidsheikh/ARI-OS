package com.arindustries.repository;

import com.arindustries.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String code);
    boolean existsByEmployeeCode(String code);

    @Query("SELECT e FROM Employee e WHERE e.active = :active AND " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.department) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(e.designation) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Employee> findByActiveAndSearch(@Param("active") boolean active, @Param("search") String search, Pageable pageable);
}
