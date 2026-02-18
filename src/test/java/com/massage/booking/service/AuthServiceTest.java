package com.massage.booking.service;

import com.massage.booking.dto.request.RegisterRequest;
import com.massage.booking.dto.response.AuthResponse;
import com.massage.booking.entity.User;
import com.massage.booking.entity.valueobject.Phone;
import com.massage.booking.exception.DuplicateResourceException;
import com.massage.booking.repository.UserRepository;
import com.massage.booking.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "John Doe",
                "+34612345678",
                "john@example.com",
                "Secure123!"
        );
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.existsByPhone(any(Phone.class))).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(jwtUtil.generateToken(any(), any())).thenReturn("token123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(validRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getType());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneExists() {
        when(userRepository.existsByPhone(any(Phone.class))).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(validRequest));

        verify(userRepository, never()).save(any());
    }
}