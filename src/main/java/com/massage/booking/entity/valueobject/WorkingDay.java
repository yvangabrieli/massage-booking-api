package com.massage.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "working_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false, unique = true)
    private Integer dayOfWeek; // 1=Monday, 7=Sunday

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    public boolean isWorkingDay() {
        return isActive;
    }
}