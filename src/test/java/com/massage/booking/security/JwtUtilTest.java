package com.massage.booking.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Must be at least 256 bits (32 characters)
    private static final String TEST_SECRET =
            "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject values (simulates @Value injection)
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_CLIENT");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldExtractPhoneFromToken() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_CLIENT");
        String phone = jwtUtil.extractPhone(token);

        assertEquals("+34612345678", phone);
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_ADMIN");
        String role = jwtUtil.extractRole(token);

        assertEquals("ROLE_ADMIN", role);
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_CLIENT");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_CLIENT");
        String tampered = token + "tampered";

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void shouldNotExpireBeforeTime() {
        String token = jwtUtil.generateToken("+34612345678", "ROLE_CLIENT");

        assertFalse(jwtUtil.isTokenExpired(token));
    }
}
