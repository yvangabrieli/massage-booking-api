package com.massage.booking.controller;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.dto.response.BookingResponse;
import com.massage.booking.entity.enums.BookingStatus;
import com.massage.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT')")
    @Operation(summary = "Create booking", description = "Authenticated users")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest request,
            @RequestParam Long clientId
    ) {
        log.info("POST /v1/bookings");
        BookingResponse response = bookingService.create(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT')")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        log.info("GET /v1/bookings/{}", id);
        BookingResponse response = bookingService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all bookings", description = "Admin only")
    public ResponseEntity<Page<BookingResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BookingStatus status
    ) {
        log.info("GET /v1/bookings");
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<BookingResponse> response = bookingService.getAll(pageable, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Get my bookings", description = "Client only")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam Long clientId
    ) {
        log.info("GET /v1/bookings/me");
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<BookingResponse> response = bookingService.getByClient(clientId, pageable, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CLIENT')")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication
    ) {
        log.info("DELETE /v1/bookings/{}", id);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String reason = body != null ? body.get("reason") : null;

        bookingService.cancel(id, clientId, isAdmin, reason);
        return ResponseEntity.noContent().build();
    }
}