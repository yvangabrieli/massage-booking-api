package com.massage.booking.service;

import com.massage.booking.dto.request.ServiceRequest;
import com.massage.booking.dto.response.ServiceResponse;
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
public class ServiceCatalogService {

    private final MassageServiceRepository serviceRepository;

    @Transactional
    public ServiceResponse.AdminServiceResponse create(ServiceRequest request) {
        log.info("Creating service: {}", request.getName());

        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Service with name already exists: " + request.getName());
        }

        com.massage.booking.entity.MassageService service = com.massage.booking.entity.MassageService.create(
                request.getName(),
                request.getCategory(),
                request.getDurationMinutes(),
                request.getCleanupMinutes(),
                request.getPrice(),
                request.getDescription()
        );

        com.massage.booking.entity.MassageService saved = serviceRepository.save(service);
        log.info("Service created with id: {}", saved.getId());
        return mapToAdminResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(Long id) {
        com.massage.booking.entity.MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
        return mapToPublicResponse(service);
    }

    @Transactional(readOnly = true)
    public ServiceResponse.AdminServiceResponse getByIdAdmin(Long id) {
        com.massage.booking.entity.MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
        return mapToAdminResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAll(ServiceCategory category, Boolean activeOnly) {
        List<com.massage.booking.entity.MassageService> services;

        if (category != null && (activeOnly == null || activeOnly)) {
            services = serviceRepository.findByCategoryAndActiveTrue(category);
        } else if (activeOnly != null && activeOnly) {
            services = serviceRepository.findByActiveTrue();
        } else {
            services = serviceRepository.findAll();
        }

        return services.stream().map(this::mapToPublicResponse).collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse.AdminServiceResponse update(Long id, ServiceRequest request) {
        com.massage.booking.entity.MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));

        service.updateDetails(
                request.getName(),
                request.getCategory(),
                request.getDurationMinutes(),
                request.getCleanupMinutes(),
                request.getPrice(),
                request.getDescription()
        );

        com.massage.booking.entity.MassageService updated = serviceRepository.save(service);
        log.info("Service updated: {}", id);
        return mapToAdminResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        com.massage.booking.entity.MassageService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
        service.deactivate();
        serviceRepository.save(service);
        log.info("Service deactivated: {}", id);
    }

    private ServiceResponse mapToPublicResponse(com.massage.booking.entity.MassageService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .category(service.getCategory())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .description(service.getDescription())
                .active(service.getActive())
                .createdAt(service.getCreatedAt())
                .build();
    }

    private ServiceResponse.AdminServiceResponse mapToAdminResponse(com.massage.booking.entity.MassageService service) {
        return ServiceResponse.AdminServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .category(service.getCategory())
                .durationMinutes(service.getDurationMinutes())
                .cleanupMinutes(service.getCleanupMinutes())
                .price(service.getPrice())
                .description(service.getDescription())
                .active(service.getActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}