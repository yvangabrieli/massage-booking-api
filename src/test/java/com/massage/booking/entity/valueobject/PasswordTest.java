package com.massage.booking.entity.valueobject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    void shouldCreatePasswordFromRaw() {
        Password password = Password.fromRaw("Secure123!", encoder);

        assertNotNull(password);
        assertNotNull(password.getHashedValue());
        assertNotEquals("Secure123!", password.getHashedValue()); // Should be hashed
    }

    @Test
    void shouldMatchCorrectPassword() {
        Password password = Password.fromRaw("Secure123!", encoder);

        assertTrue(password.matches("Secure123!", encoder));
        assertFalse(password.matches("WrongPass123!", encoder));
    }

    @Test
    void shouldCreatePasswordFromHashed() {
        String hashed = encoder.encode("Secure123!");
        Password password = Password.fromHashed(hashed);

        assertNotNull(password);
        assertEquals(hashed, password.getHashedValue());
    }

    @Test
    void shouldRejectNullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw(null, encoder));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("", encoder));
    }

    @Test
    void shouldRejectTooShortPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("Short1!", encoder)); // Only 7 chars
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("nouppercase123!", encoder));
    }

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("NOLOWERCASE123!", encoder));
    }

    @Test
    void shouldRejectPasswordWithoutDigit() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("NoDigitPassword!", encoder));
    }

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        assertThrows(IllegalArgumentException.class,
                () -> Password.fromRaw("NoSpecial123", encoder));
    }

    @Test
    void shouldCheckPasswordStrength() {
        assertEquals(Password.PasswordStrength.WEAK,
                Password.checkStrength("short"));

        assertEquals(Password.PasswordStrength.MEDIUM,
                Password.checkStrength("Medium123!"));

        assertEquals(Password.PasswordStrength.STRONG,
                Password.checkStrength("VeryStrongPassword123!@#"));
    }

    @Test
    void shouldNotExposePasswordInToString() {
        Password password = Password.fromRaw("Secure123!", encoder);
        String toString = password.toString();

        assertFalse(toString.contains("Secure123!"));
        assertTrue(toString.contains("PROTECTED"));
    }
}