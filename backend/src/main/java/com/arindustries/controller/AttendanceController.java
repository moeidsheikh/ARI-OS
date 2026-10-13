package com.arindustries.controller;

import com.arindustries.dto.request.AttendanceRequest;
import com.arindustries.dto.response.AttendanceResponse;
import com.arindustries.pagination.PageResponse;
import com.arindustries.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "Plant Shift Attendance & Punch Telemetry APIs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @Operation(summary = "Get paginated attendance logs")
    public ResponseEntity<PageResponse<AttendanceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(attendanceService.getAll(page, size, employeeId, fromDate, toDate));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Record employee check-in / mark attendance")
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody AttendanceRequest request,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(attendanceService.checkIn(request, user));
    }

    @PostMapping("/check-out/{employeeId}")
    @Operation(summary = "Record employee check-out and compute working hours")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long employeeId,
            Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(attendanceService.checkOut(employeeId, user));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get today's attendance metrics summary")
    public ResponseEntity<Map<String, Long>> getTodaySummary() {
        return ResponseEntity.ok(attendanceService.getTodaySummary());
    }
}
