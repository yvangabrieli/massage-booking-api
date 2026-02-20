package com.massage.booking.entity;

import com.massage.booking.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private MassageService service;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.BOOKED;

    private String guestName;

    private String guestPhone;

    private String canceledReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Booking create(
            Long clientId,
            Long serviceId,
            LocalDateTime startTime,
            Integer totalMinutes,
            String guestName,
            String guestPhone
    ) {
        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setServiceId(serviceId);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusMinutes(totalMinutes));
        booking.setStatus(BookingStatus.BOOKED);
        booking.setGuestName(guestName);
        booking.setGuestPhone(guestPhone);
        return booking;
    }

    public boolean canBeCanceled() {
        Duration until = Duration.between(LocalDateTime.now(), startTime);
        return until.toHours() >= 12;
    }

    public void cancel(String reason) {
        if (status == BookingStatus.CANCELED) {
            throw new IllegalStateException("Booking already canceled");
        }
        this.status = BookingStatus.CANCELED;
        this.canceledReason = reason;
    }

    public void adminCancel(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason required for admin");
        }
        this.status = BookingStatus.CANCELED;
        this.canceledReason = reason;
    }

    public void complete() {
        if (status != BookingStatus.BOOKED) {
            throw new IllegalStateException("Can only complete booked appointments");
        }
        this.status = BookingStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != BookingStatus.BOOKED) {
            throw new IllegalStateException("Can only mark booked appointments as no-show");
        }
        this.status = BookingStatus.NO_SHOW;
    }

    public boolean isActive() {
        return status == BookingStatus.BOOKED;
    }
}