package com.massage.booking.repository;

import com.massage.booking.entity.Booking;
import com.massage.booking.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByClientIdAndStatus(Long clientId, BookingStatus status, Pageable pageable);

    Page<Booking> findByClientId(Long clientId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = 'BOOKED' AND " +
            "((b.startTime < :endTime) AND (b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findByClientIdAndStartTimeAfter(Long clientId, LocalDateTime after);
}