package com.massage.booking.controller;

import com.massage.booking.dto.request.BookingRequest;
import com.massage.booking.dto.response.BookingResponse;
import com.massage.booking.entity.enums.BookingStatus;
import com.massage.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create new booking")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest request,  // Add @Valid here
            @AuthenticationPrincipal UserDetails userDetails) {

        Long clientId = extractClientId(userDetails);
        BookingResponse response = bookingService.create(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all bookings (Admin) or client bookings")
    public ResponseEntity<Page<BookingResponse>> getAll(
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable,
            @RequestParam(required = false) BookingStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<BookingResponse> bookings;
        if (isAdmin) {
            bookings = bookingService.getAll(pageable, status);
        } else {
            Long clientId = extractClientId(userDetails);
            bookings = bookingService.getByClient(clientId, pageable, status);
        }

        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long clientId = extractClientId(userDetails);

        bookingService.cancel(id, clientId, isAdmin, reason);
        return ResponseEntity.noContent().build();
    }

    private Long extractClientId(UserDetails userDetails) {
        // Implementation depends on your UserDetails structure
        // This is a placeholder - adjust based on your actual implementation
        return Long.valueOf(userDetails.getUsername());
    }
}