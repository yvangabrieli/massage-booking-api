package com.massage.booking.controller;

import com.massage.booking.dto.request.ServiceRequest;
import com.massage.booking.dto.response.ServiceResponse;
import com.massage.booking.entity.enums.ServiceCategory;
import com.massage.booking.service.MassageServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Services", description = "Massage service management endpoints")
public class ServiceController {

    private final MassageServiceService massageServiceService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create service", description = "Admin only")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        log.info("POST /v1/services");
        ServiceResponse response = massageServiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID", description = "Public endpoint")
    public ResponseEntity<ServiceResponse> getById(@PathVariable Long id) {
        log.info("GET /v1/services/{}", id);
        ServiceResponse response = massageServiceService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all services", description = "Public endpoint, can filter by category")
    public ResponseEntity<List<ServiceResponse>> getAll(
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly
    ) {
        log.info("GET /v1/services - category: {}, activeOnly: {}", category, activeOnly);
        List<ServiceResponse> response = massageServiceService.getAll(category, activeOnly);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update service", description = "Admin only")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request
    ) {
        log.info("PUT /v1/services/{}", id);
        ServiceResponse response = massageServiceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete service", description = "Admin only, soft delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /v1/services/{}", id);
        massageServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}