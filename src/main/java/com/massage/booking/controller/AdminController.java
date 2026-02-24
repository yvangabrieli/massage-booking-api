package com.massage.booking.controller;

import com.massage.booking.dto.response.AuthResponse;
import com.massage.booking.entity.User;
import com.massage.booking.exception.ResourceNotFoundException;
import com.massage.booking.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin-only role management")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

    private final UserRepository userRepository;

    @PatchMapping("/users/{id}/promote")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Promote a client to SubAdmin", description = "Admin only")
    public ResponseEntity<AuthResponse.UserInfo> promoteToSubAdmin(@PathVariable Long id) {
        log.info("Promoting user {} to SUBADMIN", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.promoteToSubAdmin();
        userRepository.save(user);

        return ResponseEntity.ok(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhoneNumber())
                .email(user.getEmailAddress())
                .role(user.getRole())
                .active(user.getActive())
                .build());
    }

    @PatchMapping("/users/{id}/demote")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Demote a SubAdmin back to Client", description = "Admin only")
    public ResponseEntity<AuthResponse.UserInfo> demoteToClient(@PathVariable Long id) {
        log.info("Demoting user {} to CLIENT", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.demoteToClient();
        userRepository.save(user);

        return ResponseEntity.ok(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhoneNumber())
                .email(user.getEmailAddress())
                .role(user.getRole())
                .active(user.getActive())
                .build());
    }
}