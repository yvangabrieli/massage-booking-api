package com.massage.booking.controller;

import com.massage.booking.service.TimeSlotService;
import com.massage.booking.service.TimeSlotService.AvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Check available time slots")
public class AvailabilityController {

    private final TimeSlotService timeSlotService;

    @GetMapping("/slots")
    @Operation(summary = "Get available slots for a specific date")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(timeSlotService.getAvailableSlotsForDate(date));
    }

    @GetMapping("/range")
    @Operation(summary = "Get availability for date range (for calendar)")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilityRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(timeSlotService.getAvailabilityForRange(startDate, endDate));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if specific date/time is available")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {

        return ResponseEntity.ok(timeSlotService.isTimeSlotAvailable(dateTime));
    }
}