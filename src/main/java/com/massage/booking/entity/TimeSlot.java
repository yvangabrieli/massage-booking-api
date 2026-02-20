package com.massage.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots", indexes = {
        @Index(name = "idx_slot_date", columnList = "slot_date"),
        @Index(name = "idx_slot_datetime", columnList = "slot_datetime", unique = true),
        @Index(name = "idx_availability", columnList = "is_available, is_blocked")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "slot_datetime", nullable = false, unique = true)
    private LocalDateTime slotDateTime;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    @Column(name = "block_reason")
    private String blockReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slotDateTime == null && slotDate != null && slotTime != null) {
            slotDateTime = LocalDateTime.of(slotDate, slotTime);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void block(String reason) {
        this.isBlocked = true;
        this.blockReason = reason;
        this.isAvailable = false;
    }

    public void unblock() {
        this.isBlocked = false;
        this.blockReason = null;
        this.isAvailable = true;
    }

    public void book() {
        this.isAvailable = false;
    }

    public void release() {
        if (!this.isBlocked) {
            this.isAvailable = true;
        }
    }
}