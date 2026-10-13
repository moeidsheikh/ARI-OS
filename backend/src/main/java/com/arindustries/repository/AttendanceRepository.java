package com.arindustries.repository;

import com.arindustries.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :from AND :to " +
           "AND (:employeeId IS NULL OR a.employee.id = :employeeId)")
    Page<Attendance> findByDateRange(@Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      @Param("employeeId") Long employeeId,
                                      Pageable pageable);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND (a.status = com.arindustries.enums.AttendanceStatus.PRESENT OR a.status = com.arindustries.enums.AttendanceStatus.LATE)")
    long countPresentOnDate(@Param("date") LocalDate date);
}
