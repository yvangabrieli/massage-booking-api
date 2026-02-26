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

    // ✅ Used by getById() and updateStatus() — already correct
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    // ✅ FIX: was findAll(pageable) — now loads client+service in one query (admin, no filter)
    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service",
            countQuery = "SELECT COUNT(b) FROM Booking b")
    Page<Booking> findAllWithDetails(Pageable pageable);

    // ✅ FIX: was findByStatus(status, pageable) — now loads client+service (admin, with filter)
    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.status = :status",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Page<Booking> findByStatusWithDetails(@Param("status") BookingStatus status, Pageable pageable);

    // ✅ FIX: was findByClientId(clientId, pageable) — now loads client+service (client view, no filter)
    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.clientId = :clientId",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.clientId = :clientId")
    Page<Booking> findByClientIdWithDetails(@Param("clientId") Long clientId, Pageable pageable);

    // ✅ FIX: was findByClientIdAndStatus() — now loads client+service (client view, with filter)
    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.client LEFT JOIN FETCH b.service WHERE b.clientId = :clientId AND b.status = :status",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.clientId = :clientId AND b.status = :status")
    Page<Booking> findByClientIdAndStatusWithDetails(@Param("clientId") Long clientId, @Param("status") BookingStatus status, Pageable pageable);

    // Keep these — still used by findByClient (list, not page) elsewhere
    List<Booking> findByClientId(Long clientId);

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end ORDER BY b.startTime")
    List<Booking> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}