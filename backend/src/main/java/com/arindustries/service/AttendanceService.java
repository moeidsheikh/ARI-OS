package com.arindustries.service;

import com.arindustries.dto.request.AttendanceRequest;
import com.arindustries.dto.response.AttendanceResponse;
import com.arindustries.entity.Attendance;
import com.arindustries.entity.Employee;
import com.arindustries.enums.AttendanceStatus;
import com.arindustries.exception.BusinessRuleException;
import com.arindustries.exception.ResourceNotFoundException;
import com.arindustries.mapper.EntityDtoMapper;
import com.arindustries.pagination.PageResponse;
import com.arindustries.repository.AttendanceRepository;
import com.arindustries.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EntityDtoMapper mapper;
    private final AuditLogService auditLogService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             EntityDtoMapper mapper,
                             AuditLogService auditLogService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getAll(int page, int size, Long employeeId, LocalDate fromDate, LocalDate toDate) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusDays(30);
        LocalDate end = toDate != null ? toDate : LocalDate.now();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "attendanceDate", "id"));
        Page<Attendance> pageResult = attendanceRepository.findByDateRange(start, end, employeeId, pageRequest);

        List<AttendanceResponse> dtos = pageResult.getContent().stream()
            .map(mapper::toAttendanceResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(dtos, pageResult.getTotalElements(), pageResult.getNumber(), pageResult.getSize());
    }

    @Transactional
    public AttendanceResponse checkIn(AttendanceRequest req, String username) {
        Employee emp = employeeRepository.findById(req.getEmployeeId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", req.getEmployeeId()));

        LocalDate date = req.getAttendanceDate() != null ? req.getAttendanceDate() : LocalDate.now();
        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), date);

        Attendance attendance;
        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            attendance = existingOpt.get();
            if (attendance.getCheckIn() != null) {
                // If already checked in, allow updating status/notes
                if (req.getStatus() != null) {
                    try { attendance.setStatus(AttendanceStatus.valueOf(req.getStatus().toUpperCase())); } catch (Exception ignored) {}
                }
                if (req.getNotes() != null) attendance.setNotes(req.getNotes());
            } else {
                attendance.setCheckIn(now);
            }
        } else {
            attendance = new Attendance();
            attendance.setEmployee(emp);
            attendance.setAttendanceDate(date);
            attendance.setCheckIn(now);

            // Determine status based on time (after 9:15 AM is LATE)
            AttendanceStatus status = AttendanceStatus.PRESENT;
            if (now.toLocalTime().isAfter(LocalTime.of(9, 15))) {
                status = AttendanceStatus.LATE;
            }
            if (req.getStatus() != null) {
                try { status = AttendanceStatus.valueOf(req.getStatus().toUpperCase()); } catch (Exception ignored) {}
            }
            attendance.setStatus(status);
            attendance.setNotes(req.getNotes());
            attendance.setMarkedBy(username != null ? username : "admin");
        }

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log(username, "CHECK_IN", "ATTENDANCE",
            "Recorded check-in for " + emp.getEmployeeCode() + " - " + emp.getFirstName(), "Attendance", saved.getId().toString());

        return mapper.toAttendanceResponse(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId, String username) {
        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        LocalDate date = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(emp.getId(), date)
            .orElseThrow(() -> new BusinessRuleException("No check-in record found for today for employee: " + emp.getEmployeeCode()));

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOut(now);

        if (attendance.getCheckIn() != null) {
            long minutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
            attendance.setWorkingHours(Math.round((minutes / 60.0) * 100.0) / 100.0);
        }

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log(username, "CHECK_OUT", "ATTENDANCE",
            "Recorded check-out for " + emp.getEmployeeCode() + " working hours: " + saved.getWorkingHours(),
            "Attendance", saved.getId().toString());

        return mapper.toAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTodaySummary() {
        LocalDate today = LocalDate.now();
        List<Attendance> todayRecords = attendanceRepository.findByAttendanceDate(today);

        long present = 0;
        long late = 0;
        long onLeave = 0;
        long absent = 0;

        for (Attendance a : todayRecords) {
            if (a.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (a.getStatus() == AttendanceStatus.LATE) late++;
            else if (a.getStatus() == AttendanceStatus.ON_LEAVE) onLeave++;
            else if (a.getStatus() == AttendanceStatus.ABSENT) absent++;
        }

        Map<String, Long> summary = new HashMap<>();
        summary.put("PRESENT", present);
        summary.put("LATE", late);
        summary.put("ON_LEAVE", onLeave);
        summary.put("ABSENT", absent);
        summary.put("TOTAL_LOGGED", (long) todayRecords.size());
        return summary;
    }
}
