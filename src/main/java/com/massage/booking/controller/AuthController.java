package com.massage.booking.controller;

import com.massage.booking.dto.request.LoginRequest;
import com.massage.booking.dto.request.RegisterRequest;
import com.massage.booking.dto.response.AuthResponse;
import com.massage.booking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new client",
            description = "Creates a new client account and returns JWT token")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /v1/auth/register - phone: {}", request.getPhone());

        AuthResponse response = authService.register(request);

        // 201 Created = resource was created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login",
            description = "Authenticate with phone and password, returns JWT token")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /v1/auth/login - phone: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        // 200 OK = success
        return ResponseEntity.ok(response);
    }
}