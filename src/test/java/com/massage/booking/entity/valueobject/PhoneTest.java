package com.massage.booking.entity.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneTest {



    @Test
    void shouldCleanPhoneNumber() {
        Phone phone = Phone.of("+34 612 345 678");

        assertEquals("+34612345678", phone.getValue());
    }

    @Test
    void shouldCleanPhoneWithDashesAndParentheses() {
        Phone phone = Phone.of("(612) 345-678");

        assertEquals("612345678", phone.getValue());
    }

    @Test
    void shouldRejectNullPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Phone.of(null));
    }

    @Test
    void shouldRejectBlankPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Phone.of(""));
    }

    @Test
    void shouldRejectTooShortPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Phone.of("12345")); // Less than 9 digits
    }

    @Test
    void shouldRejectTooLongPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Phone.of("1234567890123456")); // More than 15 digits
    }

    @Test
    void shouldRejectInvalidCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> Phone.of("abc123def"));
    }


    @Test
    void shouldBeEqualIfSameValue() {
        Phone phone1 = Phone.of("+34612345678");
        Phone phone2 = Phone.of("+34612345678");

        assertEquals(phone1, phone2);
        assertEquals(phone1.hashCode(), phone2.hashCode());
    }
}
