package com.massage.booking.service;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.dto.response.BookingResponse;
import com.massage.booking.entity.Booking;
import com.massage.booking.entity.Client;
import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.User;
import com.massage.booking.entity.enums.BookingStatus;
import com.massage.booking.entity.enums.Role;
import com.massage.booking.entity.enums.ServiceCategory;
import com.massage.booking.exception.BusinessException;
import com.massage.booking.repository.BookingRepository;
import com.massage.booking.repository.ClientRepository;
import com.massage.booking.repository.MassageServiceRepository;
import com.massage.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FIX #2: Rewritten to match the corrected BookingService.
 * Service now uses:
 *   - userRepository.findById(userId)   → load User
 *   - clientRepository.findByUserId(userId) → resolve linked Client
 * Tests now mock both repositories correctly.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private MassageServiceRepository serviceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TimeSlotService timeSlotService;
    @Mock private EmailNotificationService emailNotificationService;

    @InjectMocks private BookingService bookingService;

    private User testUser;
    private Client testClient;
    private MassageService testService;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setRole(Role.ROLE_CLIENT);
        testUser.setActive(true);

        testClient = new Client();
        testClient.setId(10L);   // different ID to catch the mismatch bug
        testClient.setName("John Doe");
        testClient.setUserId(1L);
        testClient.setActive(true);

        testService = new MassageService();
        testService.setId(1L);
        testService.setName("Toque Profundo 60");
        testService.setCategory(ServiceCategory.DEEP_TISSUE);
        testService.setDurationMinutes(60);
        testService.setCleanupMinutes(10);
        testService.setPrice(BigDecimal.valueOf(65));
        testService.setActive(true);

        validRequest = new BookingRequest();
        validRequest.setServiceId(1L);
        validRequest.setStartTime(LocalDateTime.now().plusHours(3));
    }

    @Test
    void create_shouldUseClientIdNotUserId() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(timeSlotService.isWorkingDay(any())).thenReturn(true);
        when(timeSlotService.isTimeSlotAvailable(any())).thenReturn(true);
        when(bookingRepository.findConflictingBookings(any(), any())).thenReturn(List.of());

        Booking savedBooking = Booking.create(
                testClient.getId(), // should use client.getId() = 10, NOT userId = 1
                1L,
                validRequest.getStartTime(),
                testService.getTotalMinutes(),
                null, null
        );
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        doNothing().when(timeSlotService).bookSlot(any());
        doNothing().when(emailNotificationService).sendBookingConfirmation(any(), any(), any(), any());

        // Act
        BookingResponse response = bookingService.create(1L, validRequest);

        // Assert - verify booking was saved with client.getId() (10), not userId (1)
        verify(bookingRepository).save(argThat(b -> b.getClientId().equals(10L)));
        assertThat(response).isNotNull();
    }

    @Test
    void create_shouldWorkWithNoLinkedClientRecord() {
        // A user who has no Client record (edge case — guest-style booking)
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(timeSlotService.isWorkingDay(any())).thenReturn(true);
        when(timeSlotService.isTimeSlotAvailable(any())).thenReturn(true);
        when(bookingRepository.findConflictingBookings(any(), any())).thenReturn(List.of());

        Booking savedBooking = Booking.create(1L, 1L, validRequest.getStartTime(), testService.getTotalMinutes(), null, null);
        when(bookingRepository.save(any())).thenReturn(savedBooking);
        doNothing().when(timeSlotService).bookSlot(any());
        doNothing().when(emailNotificationService).sendBookingConfirmation(any(), any(), any(), any());

        BookingResponse response = bookingService.create(1L, validRequest);
        assertThat(response).isNotNull();
    }

    @Test
    void create_shouldThrow_whenBookingTooSoon() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(timeSlotService.isWorkingDay(any())).thenReturn(true);

        validRequest.setStartTime(LocalDateTime.now().plusMinutes(30)); // too soon

        assertThatThrownBy(() -> bookingService.create(1L, validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("2 hours");
    }

    @Test
    void create_shouldThrow_whenNotWorkingDay() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(timeSlotService.isWorkingDay(any())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.create(1L, validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Thursday");
    }

    @Test
    void create_shouldThrow_whenSlotNotAvailable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(timeSlotService.isWorkingDay(any())).thenReturn(true);
        when(timeSlotService.isTimeSlotAvailable(any())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.create(1L, validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }
}
