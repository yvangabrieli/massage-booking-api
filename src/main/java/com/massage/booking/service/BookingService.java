package com.massage.booking.service;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.dto.response.BookingResponse;
import com.massage.booking.entity.Booking;
import com.massage.booking.entity.Client;
import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.enums.BookingStatus;
import com.massage.booking.exception.BusinessException;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.BookingRepository;
import com.massage.booking.repository.ClientRepository;
import com.massage.booking.repository.MassageServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final MassageServiceRepository serviceRepository;
    private final TimeSlotService timeSlotService; // NOW INCLUDED!

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse create(Long clientId, BookingRequest request) {
        log.info("Creating booking for client: {}, service: {}, time: {}",
                clientId, request.getServiceId(), request.getStartTime());

        // 1. Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        // 2. Validate service exists
        MassageService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));

        // 3. Validate business rules (2 hours advance, etc.)
        validateBookingRules(request.getStartTime());

        // 4. Check if it's a working day (Thu-Sun) using TimeSlotService
        if (!timeSlotService.isWorkingDay(request.getStartTime().toLocalDate())) {
            throw new BusinessException("We are only open Thursday through Sunday", HttpStatus.BAD_REQUEST);
        }

        // 5. Calculate end time
        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getTotalMinutes());

        // 6. Check availability using TimeSlotService AND prevent double booking
        checkAvailability(request.getStartTime(), endTime);

        // 7. Create booking
        Booking booking = Booking.create(
                clientId,
                service.getId(),
                request.getStartTime(),
                service.getTotalMinutes(),
                request.getGuestName(),
                request.getGuestPhone()
        );

        // 8. Save booking and mark slot as booked
        Booking saved;
        try {
            saved = bookingRepository.save(booking);
            // Mark the slot as booked in time_slots table
            timeSlotService.bookSlot(request.getStartTime());
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent booking attempt detected for time slot: {}", request.getStartTime());
            throw new BusinessException("Time slot was just booked by another user. Please select a different time.",
                    HttpStatus.CONFLICT);
        }

        log.info("Booking created successfully with id: {}", saved.getId());
        return mapToResponse(saved, client, service);
    }

    @Transactional
    public void cancel(Long id, Long clientId, boolean isAdmin, String reason) {
        log.info("Canceling booking: {}, admin: {}", id, isAdmin);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (!isAdmin && !booking.getClientId().equals(clientId)) {
            throw new BusinessException("Cannot cancel other client's booking", HttpStatus.FORBIDDEN);
        }

        if (isAdmin) {
            booking.adminCancel(reason);
        } else {
            if (!booking.canBeCanceled()) {
                throw new BusinessException(
                        "Cannot cancel within 12 hours of appointment",
                        HttpStatus.CONFLICT
                );
            }
            booking.cancel(null);
        }

        bookingRepository.save(booking);

        // Release the time slot
        timeSlotService.releaseSlot(booking.getStartTime());

        log.info("Booking canceled: {}", id);
    }

    private void checkAvailability(LocalDateTime startTime, LocalDateTime endTime) {
        // Check using TimeSlotService for the start time
        if (!timeSlotService.isTimeSlotAvailable(startTime)) {
            throw new BusinessException("Selected time slot is not available", HttpStatus.CONFLICT);
        }

        // Also check for conflicting bookings (backup safety)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Time slot already booked", HttpStatus.CONFLICT);
        }
    }

    private void validateBookingRules(LocalDateTime startTime) {
        Duration until = Duration.between(LocalDateTime.now(), startTime);

        if (until.toHours() < 2) {
            throw new BusinessException("Must book at least 2 hours in advance", HttpStatus.BAD_REQUEST);
        }
        if (until.toDays() > 90) {
            throw new BusinessException("Cannot book more than 3 months in advance", HttpStatus.BAD_REQUEST);
        }
    }

    // ... keep all your other existing methods (getById, getAll, getByClient, mapToResponse, etc.) ...

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        log.info("Getting booking by id: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        Client client = clientRepository.findById(booking.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", booking.getClientId()));

        MassageService service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", booking.getServiceId()));

        return mapToResponse(booking, client, service);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(Pageable pageable, BookingStatus status) {
        log.info("Getting all bookings - status: {}", status);

        Page<Booking> bookings = status != null
                ? bookingRepository.findByStatus(status, pageable)
                : bookingRepository.findAll(pageable);

        return bookings.map(this::mapToResponseSimple);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getByClient(Long clientId, Pageable pageable, BookingStatus status) {
        log.info("Getting bookings for client: {}, status: {}", clientId, status);

        Page<Booking> bookings = status != null
                ? bookingRepository.findByClientIdAndStatus(clientId, status, pageable)
                : bookingRepository.findByClientId(clientId, pageable);

        return bookings.map(this::mapToResponseSimple);
    }

    private BookingResponse mapToResponse(Booking booking, Client client, MassageService service) {
        return BookingResponse.builder()
                .id(booking.getId())
                .client(BookingResponse.ClientInfo.builder()
                        .id(client.getId())
                        .name(client.getName())
                        .phone(client.getPhoneNumber())
                        .build())
                .service(BookingResponse.ServiceInfo.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .durationMinutes(service.getDurationMinutes())
                        .build())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .guestName(booking.getGuestName())
                .guestPhone(booking.getGuestPhone())
                .canceledReason(booking.getCanceledReason())
                .canCancel(booking.canBeCanceled())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private BookingResponse mapToResponseSimple(Booking booking) {
        Client client = clientRepository.findById(booking.getClientId()).orElse(null);
        MassageService service = serviceRepository.findById(booking.getServiceId()).orElse(null);

        return BookingResponse.builder()
                .id(booking.getId())
                .client(client != null ? BookingResponse.ClientInfo.builder()
                        .id(client.getId())
                        .name(client.getName())
                        .phone(client.getPhoneNumber())
                        .build() : null)
                .service(service != null ? BookingResponse.ServiceInfo.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .durationMinutes(service.getDurationMinutes())
                        .build() : null)
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .canCancel(booking.canBeCanceled())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}