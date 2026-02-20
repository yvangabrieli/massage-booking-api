package com.massage.booking.service;

import com.massage.booking.entity.TimeSlot;
import com.massage.booking.entity.WorkingDay;
import com.massage.booking.exception.BusinessException;
import com.massage.booking.repository.TimeSlotRepository;
import com.massage.booking.repository.WorkingDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final WorkingDayRepository workingDayRepository;

    // Service duration in minutes (can be configurable)
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final LocalTime OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

    /**
     * Check if a specific date/time is available for booking
     * Respects working days (Thu-Sun) and existing bookings
     */
    public boolean isTimeSlotAvailable(LocalDateTime dateTime) {
        // 1. Check if it's a working day (Thu-Sun)
        if (!isWorkingDay(dateTime.toLocalDate())) {
            return false;
        }

        // 2. Check if within business hours
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(OPEN_TIME) || time.isAfter(CLOSE_TIME.minusMinutes(1))) {
            return false;
        }

        // 3. Check if slot exists and is available
        return timeSlotRepository.findBySlotDateTime(dateTime)
                .map(slot -> slot.getIsAvailable() && !slot.getIsBlocked())
                .orElse(true); // If slot doesn't exist, assume available (will be created on demand)
    }

    /**
     * Get available time slots for a specific date
     */
    public List<LocalDateTime> getAvailableSlotsForDate(LocalDate date) {
        // Check if working day
        if (!isWorkingDay(date)) {
            return new ArrayList<>();
        }

        // Generate or retrieve slots for this date
        List<TimeSlot> slots = timeSlotRepository.findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(date);

        if (slots.isEmpty()) {
            // Generate slots if they don't exist
            generateSlotsForDate(date);
            slots = timeSlotRepository.findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(date);
        }

        return slots.stream()
                .map(TimeSlot::getSlotDateTime)
                .collect(Collectors.toList());
    }

    /**
     * Get available slots for date range (for calendar view)
     */
    public List<AvailabilityResponse> getAvailabilityForRange(LocalDate startDate, LocalDate endDate) {
        List<AvailabilityResponse> availability = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            boolean isWorking = isWorkingDay(current);
            List<LocalDateTime> slots = isWorking ? getAvailableSlotsForDate(current) : new ArrayList<>();

            availability.add(AvailabilityResponse.builder()
                    .date(current)
                    .isWorkingDay(isWorking)
                    .availableSlots(slots)
                    .build());

            current = current.plusDays(1);
        }

        return availability;
    }

    /**
     * Block a specific time slot (for admin)
     */
    @Transactional
    public void blockTimeSlot(LocalDateTime dateTime, String reason) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseGet(() -> createSlot(dateTime));

        slot.block(reason);
        timeSlotRepository.save(slot);
        log.info("Blocked time slot: {} - Reason: {}", dateTime, reason);
    }

    /**
     * Unblock a time slot
     */
    @Transactional
    public void unblockTimeSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseThrow(() -> new BusinessException("Time slot not found", HttpStatus.NOT_FOUND));

        slot.unblock();
        timeSlotRepository.save(slot);
        log.info("Unblocked time slot: {}", dateTime);
    }

    /**
     * Mark slot as booked (called when booking is created)
     */
    @Transactional
    public void bookSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseGet(() -> createSlot(dateTime));

        if (!slot.getIsAvailable() || slot.getIsBlocked()) {
            throw new BusinessException("Time slot is not available", HttpStatus.CONFLICT);
        }

        slot.book();
        timeSlotRepository.save(slot);
        log.info("Booked time slot: {}", dateTime);
    }

    /**
     * Release slot (called when booking is cancelled)
     */
    @Transactional
    public void releaseSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseThrow(() -> new BusinessException("Time slot not found", HttpStatus.NOT_FOUND));

        slot.release();
        timeSlotRepository.save(slot);
        log.info("Released time slot: {}", dateTime);
    }

    /**
     * Check if date is a working day (Thu-Sun)
     */
    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // 1=Monday, 7=Sunday

        // Check if it's Thursday(4), Friday(5), Saturday(6), or Sunday(7)
        return workingDayRepository.existsByDayOfWeekAndIsActiveTrue(dayValue);
    }

    /**
     * Generate 30-minute slots for a specific date
     */
    @Transactional
    public void generateSlotsForDate(LocalDate date) {
        if (!isWorkingDay(date)) {
            return;
        }

        LocalTime currentTime = OPEN_TIME;

        while (currentTime.isBefore(CLOSE_TIME)) {
            LocalDateTime dateTime = LocalDateTime.of(date, currentTime);

            if (!timeSlotRepository.existsBySlotDateTime(dateTime)) {
                TimeSlot slot = TimeSlot.builder()
                        .slotDate(date)
                        .slotTime(currentTime)
                        .slotDateTime(dateTime)
                        .isAvailable(true)
                        .isBlocked(false)
                        .build();
                timeSlotRepository.save(slot);
            }

            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
        }

        log.info("Generated slots for date: {}", date);
    }

    /**
     * Generate slots for next 3 months (run on startup or schedule)
     */
    @Transactional
    public void generateUpcomingSlots() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (isWorkingDay(current)) {
                generateSlotsForDate(current);
            }
            current = current.plusDays(1);
        }
    }

    private TimeSlot createSlot(LocalDateTime dateTime) {
        return TimeSlot.builder()
                .slotDate(dateTime.toLocalDate())
                .slotTime(dateTime.toLocalTime())
                .slotDateTime(dateTime)
                .isAvailable(true)
                .isBlocked(false)
                .build();
    }

    // DTO for availability response
    @lombok.Builder
    @lombok.Data
    public static class AvailabilityResponse {
        private LocalDate date;
        private boolean isWorkingDay;
        private List<LocalDateTime> availableSlots;
    }
}