package com.massage.booking.entity.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object for Email
 * DDD Pattern: Encapsulates validation and ensures email is always valid
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Column(name = "email", length = 100)
    private String value;

    // Private constructor - forces use of factory method
    private Email(String value) {
        this.value = value;
    }

    /**
     * Factory method - the ONLY way to create an Email
     * DDD: Validates on creation
     */
    public static Email of(String email) {
        // Email is optional
        if (email == null || email.isBlank()) {
            return null;
        }

        String trimmed = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        return new Email(trimmed);
    }

    /**
     * Get the email value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get domain part of email
     */
    public String getDomain() {
        if (value == null) return null;
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : null;
    }

    /**
     * Check if email is from a specific domain
     */
    public boolean isFromDomain(String domain) {
        String emailDomain = getDomain();
        return emailDomain != null && emailDomain.equalsIgnoreCase(domain);
    }

    /**
     * Value Objects are equal if their values are equal (not identity)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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