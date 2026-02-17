package com.massage.booking.entity.valueobject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object for Password
 * DDD Pattern: Encapsulates validation and hashing logic
 *
 * Note: NOT @Embeddable because we store the hashed value in User entity
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    // Must contain: uppercase, lowercase, digit, special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
    );

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    private String hashedValue;

    // Private constructor
    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    /**
     * Create password from raw (plain text) password
     * Use this for user registration
     */
    public static Password fromRaw(String rawPassword, PasswordEncoder encoder) {
        validateRawPassword(rawPassword);
        String hashed = encoder.encode(rawPassword);
        return new Password(hashed);
    }

    /**
     * Create password from already hashed password
     * Use this when loading from database
     */
    public static Password fromHashed(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be empty");
        }
        return new Password(hashedPassword);
    }

    /**
     * Validate raw password meets all requirements
     */
    private static void validateRawPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }

        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must not exceed " + MAX_LENGTH + " characters"
            );
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter, " +
                            "one lowercase letter, one digit, and one special character (@$!%*?&#)"
            );
        }
    }

    /**
     * Check if a raw password matches this hashed password
     */
    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        if (rawPassword == null) return false;
        return encoder.matches(rawPassword, hashedValue);
    }

    /**
     * Get the hashed value (for storing in database)
     */
    public String getHashedValue() {
        return hashedValue;
    }

    /**
     * Check password strength (optional feature)
     */
    public static PasswordStrength checkStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        if (rawPassword.length() >= 12) score++;
        if (rawPassword.matches(".*[a-z].*")) score++;
        if (rawPassword.matches(".*[A-Z].*")) score++;
        if (rawPassword.matches(".*\\d.*")) score++;
        if (rawPassword.matches(".*[@$!%*?&#].*")) score++;
        if (rawPassword.length() >= 16) score++;

        if (score <= 3) return PasswordStrength.WEAK;
        if (score <= 5) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    /**
     * Password strength levels
     */
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    /**
     * Never expose the password value!
     */
    @Override
    public String toString() {
        return "Password{PROTECTED}";
    }
}