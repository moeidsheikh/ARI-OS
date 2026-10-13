package com.arindustries.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class AttendanceRequest {
    @NotNull private Long employeeId;
    @NotNull private LocalDate attendanceDate;
    @NotBlank private String status;
    private String checkInTime;
    private String notes;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long v) { this.employeeId = v; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate v) { this.attendanceDate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String v) { this.checkInTime = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
