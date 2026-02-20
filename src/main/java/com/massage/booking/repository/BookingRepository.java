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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.status = 'BOOKED' AND " +
            "b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findConflictingBookings(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    List<Booking> findByClientId(Long clientId);

    Page<Booking> findByClientId(Long clientId, Pageable pageable);

    Page<Booking> findByClientIdAndStatus(Long clientId, BookingStatus status, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end ORDER BY b.startTime")
    List<Booking> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}