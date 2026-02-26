package com.massage.booking.service;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.dto.response.BookingResponse;
import com.massage.booking.entity.Booking;
import com.massage.booking.entity.Client;
import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.User;
import com.massage.booking.entity.enums.BookingStatus;
import com.massage.booking.exception.BusinessException;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.BookingRepository;
import com.massage.booking.repository.ClientRepository;
import com.massage.booking.repository.MassageServiceRepository;
import com.massage.booking.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final TimeSlotService timeSlotService;
    private final EmailNotificationService emailNotificationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse create(Long clientIdFromAuth, BookingRequest request) {
        log.info("Creating booking for clientId: {}, service: {}, time: {}",
                clientIdFromAuth, request.getServiceId(), request.getStartTime());

        // 1️⃣ Ensure Client exists — create if missing
        Client client = clientRepository.findById(clientIdFromAuth)
                .orElseGet(() -> {
                    // Create a new client if not found
                    Client newClient = Client.create(
                            request.getGuestName(),     // name from request
                            request.getGuestPhone(),    // phone from request
                            request.getGuestEmail(),    // email from request
                            null,                       // birthday unknown
                            null,                       // notes
                            null                        // other field if any
                    );
                    newClient.setId(clientIdFromAuth); // link to auth id if needed
                    return clientRepository.save(newClient);
                });

        // 2️⃣ Load Service
        MassageService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));

        // 3️⃣ Validate booking rules
        validateBookingRules(request.getStartTime());

        if (!timeSlotService.isWorkingDay(request.getStartTime().toLocalDate())) {
            throw new BusinessException("We are only open Thursday through Sunday", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime endTime = request.getStartTime().plusMinutes(service.getTotalMinutes());
        checkAvailability(request.getStartTime(), endTime);

        // 4️⃣ Create Booking with real clientId
        Booking booking = Booking.create(
                client.getId(),          // guaranteed to exist
                service.getId(),
                request.getStartTime(),
                service.getTotalMinutes(),
                request.getGuestName(),
                request.getGuestPhone()
        );

        Booking saved;
        try {
            saved = bookingRepository.save(booking);
            timeSlotService.bookSlot(request.getStartTime());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                    "Time slot was just booked by another user. Please select a different time.",
                    HttpStatus.CONFLICT
            );
        }

        // 5️⃣ Send confirmation email safely
        try {
            emailNotificationService.sendBookingConfirmation(
                    client.getEmail()  != null ? client.getEmail().getValue() : null,     // email from client
                    client.getName(),         // name from client
                    service.getName(),
                    request.getStartTime()
            );
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for client {}: {}", client.getId(), e.getMessage());
            // Do NOT rollback booking if email fails
        }

        // 6️⃣ Log success
        log.info("Booking created successfully with id: {}", saved.getId());

        // 7️⃣ Return response
        return mapToResponse(saved, client, service);
    }

    @Transactional
    public void cancel(Long id, Long userId, boolean isAdmin, String reason) {
        log.info("Canceling booking: {}, admin: {}", id, isAdmin);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        Client callerClient = clientRepository.findByUserId(userId).orElse(null);
        Long callerClientId = (callerClient != null) ? callerClient.getId() : userId;

        if (!isAdmin && !booking.getClientId().equals(callerClientId)) {
            throw new BusinessException("Cannot cancel other client's booking", HttpStatus.FORBIDDEN);
        }

        MassageService service = serviceRepository.findById(booking.getServiceId()).orElse(null);
        Client ownerClient = clientRepository.findById(booking.getClientId()).orElse(null);
        User ownerUser = (ownerClient != null && ownerClient.getUserId() != null)
                ? userRepository.findById(ownerClient.getUserId()).orElse(null)
                : null;

        if (isAdmin) {
            booking.adminCancel(reason);
        } else {
            if (!booking.canBeCanceled()) {
                throw new BusinessException("Cannot cancel within 12 hours of appointment", HttpStatus.CONFLICT);
            }
            booking.cancel(null);
        }

        bookingRepository.save(booking);
        timeSlotService.releaseSlot(booking.getStartTime());

        if (ownerUser != null && service != null) {
            emailNotificationService.sendBookingCancellation(
                    ownerUser.getEmailAddress(),
                    ownerUser.getName(),
                    service.getName(),
                    booking.getStartTime(),
                    reason
            );
        }

        log.info("Booking canceled: {}", id);
    }

    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatus newStatus) {
        log.info("Updating booking {} status to {}", id, newStatus);

        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        switch (newStatus) {
            case COMPLETED -> booking.complete();
            case NO_SHOW -> booking.markNoShow();
            case CANCELED -> booking.cancel("Cancelled by admin");
            default -> throw new BusinessException("Invalid status transition", HttpStatus.BAD_REQUEST);
        }

        bookingRepository.save(booking);
        return mapToResponse(booking, booking.getClient(), booking.getService());
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return mapToResponse(booking, booking.getClient(), booking.getService());
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(Pageable pageable, BookingStatus status) {
        // ✅ FIX: use JOIN FETCH queries so client+service are never null
        Page<Booking> bookings = status != null
                ? bookingRepository.findByStatusWithDetails(status, pageable)
                : bookingRepository.findAllWithDetails(pageable);
        return bookings.map(this::mapToResponseFromJoin);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getByClient(Long userId, Pageable pageable, BookingStatus status) {
        // Resolve user ID → client ID
        Long resolvedClientId = clientRepository.findByUserId(userId)
                .map(Client::getId)
                .orElse(userId);

        // ✅ FIX: use JOIN FETCH queries so client+service are never null
        Page<Booking> bookings = status != null
                ? bookingRepository.findByClientIdAndStatusWithDetails(resolvedClientId, status, pageable)
                : bookingRepository.findByClientIdWithDetails(resolvedClientId, pageable);
        return bookings.map(this::mapToResponseFromJoin);
    }

    private void checkAvailability(LocalDateTime startTime, LocalDateTime endTime) {
        if (!timeSlotService.isTimeSlotAvailable(startTime)) {
            throw new BusinessException("Selected time slot is not available", HttpStatus.CONFLICT);
        }
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

    private BookingResponse mapToResponse(Booking booking, Client client, MassageService service) {
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
                .guestName(booking.getGuestName())
                .guestPhone(booking.getGuestPhone())
                .canceledReason(booking.getCanceledReason())
                .canCancel(booking.canBeCanceled())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private BookingResponse mapToResponseFromJoin(Booking booking) {
        return mapToResponse(booking, booking.getClient(), booking.getService());
    }
}