package com.massage.booking.repository;

import com.massage.booking.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findBySlotDateAndIsAvailableTrueAndIsBlockedFalse(LocalDate date);

    Optional<TimeSlot> findBySlotDateTime(LocalDateTime dateTime);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.slotDate BETWEEN :startDate AND :endDate " +
            "AND ts.isAvailable = true AND ts.isBlocked = false " +
            "ORDER BY ts.slotDateTime")
    List<TimeSlot> findAvailableSlotsInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsBySlotDateTime(LocalDateTime dateTime);
}