package com.massage.booking.service;

import com.massage.booking.dto.request.ServiceRequest;
import com.massage.booking.dto.response.ServiceResponse;
import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.enums.ServiceCategory;
import com.massage.booking.exception.DuplicateResourceException;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.MassageServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MassageServiceService {

    private final MassageServiceRepository serviceRepository;

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        log.info("Creating service: {}", request.getName());

        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "Service with name already exists: " + request.getName()
            );
        }

        MassageService service = MassageService.create(
                request.getName(),
                request.getCategory(),
                request.getDurationMinutes(),
                request.getCleanupMinutes(),
                request.getDescription()
        );

        MassageService saved = serviceRepository.save(service);
        log.info("Service created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(Long id) {
        log.info("Getting service by id: {}", id);

        MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        return mapToResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAll(ServiceCategory category, Boolean activeOnly) {
        log.info("Getting all services - category: {}, activeOnly: {}", category, activeOnly);

        List<MassageService> services;

        if (category != null && (activeOnly == null || activeOnly)) {
            services = serviceRepository.findByCategoryAndActiveTrue(category);
        } else if (activeOnly != null && activeOnly) {
            services = serviceRepository.findByActiveTrue();
        } else {
            services = serviceRepository.findAll();
        }

        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceRequest request) {
        log.info("Updating service id: {}", id);

        MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        service.updateDetails(
                request.getName(),
                request.getCategory(),
                request.getDurationMinutes(),
                request.getCleanupMinutes(),
                request.getDescription()
        );

        MassageService updated = serviceRepository.save(service);
        log.info("Service updated: {}", id);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting service id: {}", id);

        MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        service.deactivate();
        serviceRepository.save(service);

        log.info("Service deactivated: {}", id);
    }

    private ServiceResponse mapToResponse(MassageService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .category(service.getCategory())
                .durationMinutes(service.getDurationMinutes())
                .cleanupMinutes(service.getCleanupMinutes())
                .description(service.getDescription())
                .active(service.getActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}