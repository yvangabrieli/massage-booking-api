package com.massage.booking.entity.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = Email.of("john@example.com");

        assertNotNull(email);
        assertEquals("john@example.com", email.getValue());
    }

    @Test
    void shouldNormalizeEmailToLowercase() {
        Email email = Email.of("John.Doe@EXAMPLE.COM");

        assertEquals("john.doe@example.com", email.getValue());
    }

    @Test
    void shouldTrimWhitespace() {
        Email email = Email.of("  john@example.com  ");

        assertEquals("john@example.com", email.getValue());
    }

    @Test
    void shouldReturnNullForBlankEmail() {
        Email email = Email.of("");

        assertNull(email);
    }

    @Test
    void shouldReturnNullForNullEmail() {
        Email email = Email.of(null);

        assertNull(email);
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> Email.of("not-an-email"));

        assertThrows(IllegalArgumentException.class,
                () -> Email.of("missing@domain"));

        assertThrows(IllegalArgumentException.class,
                () -> Email.of("@example.com"));
    }

    @Test
    void shouldExtractDomain() {
        Email email = Email.of("john@example.com");

        assertEquals("example.com", email.getDomain());
    }

    @Test
    void shouldCheckDomain() {
        Email email = Email.of("john@example.com");

        assertTrue(email.isFromDomain("example.com"));
        assertFalse(email.isFromDomain("other.com"));
    }

    @Test
    void shouldBeEqualIfSameValue() {
        Email email1 = Email.of("john@example.com");
        Email email2 = Email.of("john@example.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }
}
