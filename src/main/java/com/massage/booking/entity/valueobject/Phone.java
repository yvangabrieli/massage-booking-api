package com.massage.booking.entity.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object for Phone Number
 * DDD Pattern: Encapsulates validation and ensures phone is always valid
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA
public class Phone {

    // Supports international format: +34612345678 or local: 612345678
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{9,15}$"
    );

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String value;

    // Private constructor - forces use of factory method
    private Phone(String value) {
        this.value = value;
    }

    /**
     * Factory method - the ONLY way to create a Phone
     * DDD: Validates on creation
     */
    public static Phone of(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Clean the phone: remove spaces, dashes, parentheses
        String cleaned = phone.replaceAll("[\\s()\\-]", "");

        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone format. Expected format: +34612345678 or 612345678. Got: " + phone
            );
        }

        return new Phone(cleaned);
    }

    /**
     * Get the phone value
     */
    public String getValue() {
        return value;
    }
    /**
     * Value Objects are equal if their values are equal (not identity)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(value, phone.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}