package com.arindustries;

import com.arindustries.enums.AttendanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class AttendanceWorkingHoursTest {

    @Test
    @DisplayName("Working hours calculation between check-in and check-out")
    void testWorkingHoursCalculation() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 10, 10, 8, 52);
        LocalDateTime checkOut = LocalDateTime.of(2026, 10, 10, 17, 32);

        long minutes = Duration.between(checkIn, checkOut).toMinutes();
        assertEquals(520, minutes); // 8 hours 40 minutes

        double hours = Math.round((minutes / 60.0) * 100.0) / 100.0;
        assertEquals(8.67, hours);
    }

    @Test
    @DisplayName("Punctuality evaluation: Check-in after 09:15 AM marked as LATE")
    void testPunctualityStatus() {
        LocalTime normalShiftStart = LocalTime.of(9, 15);

        LocalTime onTimePunch = LocalTime.of(9, 5);
        AttendanceStatus status1 = onTimePunch.isAfter(normalShiftStart) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        assertEquals(AttendanceStatus.PRESENT, status1);

        LocalTime latePunch = LocalTime.of(9, 25);
        AttendanceStatus status2 = latePunch.isAfter(normalShiftStart) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        assertEquals(AttendanceStatus.LATE, status2);
    }
}
