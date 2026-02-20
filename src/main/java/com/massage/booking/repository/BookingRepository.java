package com.massage.booking.repository;

import com.massage.booking.entity.Booking;
import com.massage.booking.entity.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // PESSIMISTIC_WRITE locks rows until transaction commits
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.status = 'BOOKED' AND " +
            "b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findConflictingBookings(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Alternative: Use native query with FOR UPDATE (more explicit)
    @Query(value = "SELECT * FROM bookings b WHERE b.status = 'BOOKED' AND " +
            "b.start_time < :endTime AND b.end_time > :startTime " +
            "FOR UPDATE", nativeQuery = true)
    List<Booking> findConflictingBookingsNative(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Optimized query with JOIN FETCH to prevent N+1
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    // Existing methods...
    List<Booking> findByClientId(Long clientId);

    Page<Booking> findByClientId(Long clientId, Pageable pageable);

    Page<Booking> findByClientIdAndStatus(Long clientId, BookingStatus status, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end ORDER BY b.startTime")
    List<Booking> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}