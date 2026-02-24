package com.massage.booking.entity;

import com.massage.booking.entity.enums.ServiceCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MassageService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCategory category;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer cleanupMinutes = 10;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

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

    public static MassageService create(
            String name,
            ServiceCategory category,
            Integer durationMinutes,
            Integer cleanupMinutes,
            BigDecimal price,
            String description
    ) {
        validateDuration(durationMinutes);
        validateCleanup(cleanupMinutes);
        validatePrice(price);

        MassageService service = new MassageService();
        service.setName(name);
        service.setCategory(category);
        service.setDurationMinutes(durationMinutes);
        service.setCleanupMinutes(cleanupMinutes != null ? cleanupMinutes : 10);
        service.setPrice(price);
        service.setDescription(description);
        service.setActive(true);
        return service;
    }

    private static void validateDuration(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }
        if (minutes > 300) {
            throw new IllegalArgumentException("Duration cannot exceed 300 minutes");
        }
    }

    private static void validateCleanup(Integer minutes) {
        if (minutes != null && minutes < 0) {
            throw new IllegalArgumentException("Cleanup time cannot be negative");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }
    }

    public void updateDetails(
            String name,
            ServiceCategory category,
            Integer durationMinutes,
            Integer cleanupMinutes,
            BigDecimal price,
            String description
    ) {
        if (durationMinutes != null) {
            validateDuration(durationMinutes);
            this.durationMinutes = durationMinutes;
        }
        if (cleanupMinutes != null) {
            validateCleanup(cleanupMinutes);
            this.cleanupMinutes = cleanupMinutes;
        }
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
        this.description = description;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Service already deactivated");
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Service already active");
        }
        this.active = true;
    }

    public Integer getTotalMinutes() {
        return durationMinutes + cleanupMinutes;
    }
}