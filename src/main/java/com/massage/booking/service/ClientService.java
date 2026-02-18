package com.massage.booking.service;

import com.massage.booking.dto.request.ClientRequest;
import com.massage.booking.dto.response.ClientResponse;
import com.massage.booking.entity.Client;
import com.massage.booking.entity.valueobject.Phone;
import com.massage.booking.exception.DuplicateResourceException;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse create(ClientRequest request) {
        log.info("Creating client with phone: {}", request.getPhone());

        Phone phone = Phone.of(request.getPhone());

        if (clientRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    "Client with phone already exists: " + request.getPhone()
            );
        }

        Client client = Client.create(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getBirthday(),
                request.getNotes(),
                null
        );

        Client saved = clientRepository.save(client);
        log.info("Client created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        log.info("Getting client by id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        return mapToResponse(client);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getAll(Pageable pageable, Boolean active, String search) {
        log.info("Getting all clients - page: {}, active: {}, search: {}",
                pageable.getPageNumber(), active, search);

        Page<Client> clients;

        if (search != null && !search.isBlank()) {
            clients = clientRepository.searchClients(search, active, pageable);
        } else if (active != null && active) {
            clients = clientRepository.findByActiveTrue(pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }

        return clients.map(this::mapToResponse);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        log.info("Updating client id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        client.updateProfile(
                request.getName(),
                request.getEmail(),
                request.getBirthday(),
                request.getNotes()
        );

        Client updated = clientRepository.save(client);
        log.info("Client updated: {}", id);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting client id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));

        client.deactivate();
        clientRepository.save(client);

        log.info("Client deactivated: {}", id);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhoneNumber())
                .email(client.getEmailAddress())
                .birthday(client.getBirthday())
                .notes(client.getNotes())
                .active(client.getActive())
                .userId(client.getUserId())
                .createdAt(client.getCreatedAt())
                .build();
    }
}