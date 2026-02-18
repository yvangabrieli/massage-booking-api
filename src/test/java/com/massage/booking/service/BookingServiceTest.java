package com.massage.booking.service;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.entity.Booking;
import com.massage.booking.entity.Client;
import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.enums.ServiceCategory;
import com.massage.booking.exception.BusinessException;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.BookingRepository;
import com.massage.booking.repository.ClientRepository;
import com.massage.booking.repository.MassageServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MassageServiceRepository serviceRepository;

    @InjectMocks
    private BookingService bookingService;

    private Client testClient;
    private MassageService testService;
    private BookingRequest validRequest;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("John Doe");

        testService = new MassageService();
        testService.setId(1L);
        testService.setName("Toque Profundo 60");
        testService.setCategory(ServiceCategory.DEEP_TISSUE);
        testService.setDurationMinutes(60);
        testService.setCleanupMinutes(10);

        validRequest = new BookingRequest();
        validRequest.setServiceId(1L);
        validRequest.setStartTime(LocalDateTime.now().plusHours(3));
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(bookingRepository.findConflictingBookings(any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = bookingService.create(1L, validRequest);

        assertNotNull(response);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowExceptionWhenClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.create(1L, validRequest));
    }

    @Test
    void shouldThrowExceptionWhenBookingTooSoon() {
        validRequest.setStartTime(LocalDateTime.now().plusMinutes(30));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        assertThrows(BusinessException.class,
                () -> bookingService.create(1L, validRequest));
    }

    @Test
    void shouldThrowExceptionWhenTimeSlotConflict() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(bookingRepository.findConflictingBookings(any(), any()))
                .thenReturn(List.of(new Booking()));

        assertThrows(BusinessException.class,
                () -> bookingService.create(1L, validRequest));
    }
}