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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    @Transactional
    public BookingResponse create(Long clientId, BookingRequest request) {
        log.info("Creating booking for client: {}, service: {}", clientId, request.getServiceId());

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        MassageService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));

        validate2HourRule(request.getStartTime());

        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getTotalMinutes());

        checkAvailability(request.getStartTime(), endTime);

        Booking booking = Booking.create(
                clientId,
                service.getId(),
                request.getStartTime(),
                service.getTotalMinutes(),
                request.getGuestName(),
                request.getGuestPhone()
        );

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with id: {}", saved.getId());

        return mapToResponse(saved, client, service);
    }

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
        log.info("Booking canceled: {}", id);
    }

    private void validate2HourRule(LocalDateTime startTime) {
        Duration until = Duration.between(LocalDateTime.now(), startTime);
        if (until.toHours() < 2) {
            throw new BusinessException(
                    "Must book at least 2 hours in advance",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void checkAvailability(LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Time slot already booked", HttpStatus.CONFLICT);
        }
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