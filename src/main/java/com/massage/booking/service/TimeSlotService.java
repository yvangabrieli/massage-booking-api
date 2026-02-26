package com.massage.booking.service;

import com.massage.booking.entity.TimeSlot;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final WorkingDayRepository workingDayRepository;

    private static final int SLOT_DURATION_MINUTES = 30;
    // FIX #5: align with DB schema (database-schema.sql sets open_time = 10:00)
    private static final LocalTime OPEN_TIME  = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(20, 0);

    public boolean isTimeSlotAvailable(LocalDateTime dateTime) {
        if (!isWorkingDay(dateTime.toLocalDate())) return false;

        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(OPEN_TIME) || time.isAfter(CLOSE_TIME.minusMinutes(1))) return false;

        return timeSlotRepository.findBySlotDateTime(dateTime)
                .map(slot -> slot.getIsAvailable() && !slot.getIsBlocked())
                .orElse(true);
    }

    public List<LocalDateTime> getAvailableSlotsForDate(LocalDate date) {
        if (!isWorkingDay(date)) return new ArrayList<>();

        List<TimeSlot> slots = timeSlotRepository.findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(date);
        if (slots.isEmpty()) {
            generateSlotsForDate(date);
            slots = timeSlotRepository.findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(date);
        }
        return slots.stream().map(TimeSlot::getSlotDateTime).collect(Collectors.toList());
    }

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

    @Transactional
    public void blockTimeSlot(LocalDateTime dateTime, String reason) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseGet(() -> createSlot(dateTime));
        slot.block(reason);
        timeSlotRepository.save(slot);
    }

    @Transactional
    public void unblockTimeSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseThrow(() -> new BusinessException("Time slot not found", HttpStatus.NOT_FOUND));
        slot.unblock();
        timeSlotRepository.save(slot);
    }

    @Transactional
    public void bookSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseGet(() -> createSlot(dateTime));
        if (!slot.getIsAvailable() || slot.getIsBlocked()) {
            throw new BusinessException("Time slot is not available", HttpStatus.CONFLICT);
        }
        slot.book();
        timeSlotRepository.save(slot);
    }

    @Transactional
    public void releaseSlot(LocalDateTime dateTime) {
        TimeSlot slot = timeSlotRepository.findBySlotDateTime(dateTime)
                .orElseThrow(() -> new BusinessException("Time slot not found", HttpStatus.NOT_FOUND));
        slot.release();
        timeSlotRepository.save(slot);
    }

    public boolean isWorkingDay(LocalDate date) {
        int dayValue = date.getDayOfWeek().getValue();
        return workingDayRepository.existsByDayOfWeekAndIsActiveTrue(dayValue);
    }

    /**
     * FIX #9: Batch insert instead of N+1 individual saves.
     * Fetches existing datetimes for the date in one query, then batch-saves only new slots.
     */
    @Transactional
    public void generateSlotsForDate(LocalDate date) {
        if (!isWorkingDay(date)) return;

        // Load all existing slot datetimes for this date in ONE query
        Set<LocalDateTime> existing = timeSlotRepository
                .findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(date)
                .stream()
                .map(TimeSlot::getSlotDateTime)
                .collect(Collectors.toSet());

        List<TimeSlot> toSave = new ArrayList<>();
        LocalTime currentTime = OPEN_TIME;
        while (currentTime.isBefore(CLOSE_TIME)) {
            LocalDateTime dateTime = LocalDateTime.of(date, currentTime);
            if (!existing.contains(dateTime) && !timeSlotRepository.existsBySlotDateTime(dateTime)) {
                toSave.add(TimeSlot.builder()
                        .slotDate(date)
                        .slotTime(currentTime)
                        .slotDateTime(dateTime)
                        .isAvailable(true)
                        .isBlocked(false)
                        .build());
            }
            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
        }

        if (!toSave.isEmpty()) {
            timeSlotRepository.saveAll(toSave); // single batch insert
            log.info("Generated {} slots for date: {}", toSave.size(), date);
        }
    }

    /**
     * FIX #9: Generate upcoming slots — still iterates dates but each date is ONE batch insert.
     */
    @Transactional
    public void generateUpcomingSlots() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        int totalGenerated = 0;

        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (isWorkingDay(current)) {
                generateSlotsForDate(current);
                totalGenerated++;
            }
            current = current.plusDays(1);
        }
        log.info("Slot generation complete for {} working days", totalGenerated);
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

    @lombok.Builder
    @lombok.Data
    public static class AvailabilityResponse {
        private LocalDate date;
        private boolean isWorkingDay;
        private List<LocalDateTime> availableSlots;
    }
}
