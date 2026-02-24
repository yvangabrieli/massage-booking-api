package com.massage.booking.controller;

import com.massage.booking.dto.request.ClientRequest;
import com.massage.booking.dto.response.ClientResponse;
import com.massage.booking.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clients", description = "Client management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create client", description = "Admin only")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        log.info("POST /v1/clients");
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUBADMIN')")
    @Operation(summary = "Get client by ID", description = "Admin and SubAdmin")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        log.info("GET /v1/clients/{}", id);
        return ResponseEntity.ok(clientService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUBADMIN')")
    @Operation(summary = "Get all clients", description = "Admin and SubAdmin, paginated")
    public ResponseEntity<Page<ClientResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        log.info("GET /v1/clients - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(clientService.getAll(pageable, active, search));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update client", description = "Admin only")
    public ResponseEntity<ClientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request
    ) {
        log.info("PUT /v1/clients/{}", id);
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete client", description = "Admin only, soft delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /v1/clients/{}", id);
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}