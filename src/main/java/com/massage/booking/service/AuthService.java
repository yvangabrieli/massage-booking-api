package com.massage.booking.service;

import com.massage.booking.dto.request.LoginRequest;
import com.massage.booking.dto.request.RegisterRequest;
import com.massage.booking.dto.response.AuthResponse;
import com.massage.booking.entity.valueobject.Email;
import com.massage.booking.entity.valueobject.Phone;
import com.massage.booking.entity.User;
import com.massage.booking.exception.DuplicateResourceException;
import com.massage.booking.exception.UnauthorizedException;
import com.massage.booking.repository.UserRepository;
import com.massage.booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ──────────────────────────────────────────
    // REGISTER
    // ──────────────────────────────────────────


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with phone: {}", request.getPhone());

        // Step 1: Check phone not already taken
        // Phone Value Object validates format automatically
        Phone phone = Phone.of(request.getPhone());

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException(
                    "Phone number already registered: " + request.getPhone()
            );
        }

        // Step 2: Check email not already taken (if provided)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Email email = Email.of(request.getEmail());
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException(
                        "Email already registered: " + request.getEmail()
                );
            }
        }

        // Step 3: Create user using our DDD factory method
        // Value Objects validate: email format, phone format, password strength
        User user = User.createClient(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getPassword(),
                passwordEncoder
        );

        // Step 4: Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Step 5 & 6: Generate token and return response
        return buildAuthResponse(savedUser);
    }

    // ──────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for phone: {}", request.getPhone());

        // Step 1: Find user by phone
        Phone phone = Phone.of(request.getPhone());

        User user = userRepository
                .findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("Login failed - phone not found: {}", request.getPhone());
                    // ✅ Generic message - don't reveal if phone exists!
                    return new UnauthorizedException("Invalid phone or password");
                });

        // Step 2: Check user is active
        if (!user.canLogin()) {
            log.warn("Login failed - user deactivated: {}", request.getPhone());
            throw new UnauthorizedException("Account is deactivated");
        }

        // Step 3: Verify password using our Value Object method
        if (!user.checkPassword(request.getPassword(), passwordEncoder)) {
            log.warn("Login failed - wrong password: {}", request.getPhone());
            // ✅ Same generic message for security!
            throw new UnauthorizedException("Invalid phone or password");
        }

        log.info("Login successful for user: {}", user.getId());

        // Step 4 & 5: Generate token and return response
        return buildAuthResponse(user);
    }

    // ──────────────────────────────────────────
    // PRIVATE HELPERS
    // ──────────────────────────────────────────

    /**
     * Build the auth response with token and user info
     * Reused by both register and login
     */
    private AuthResponse buildAuthResponse(User user) {
        // Generate JWT token
        String token = jwtUtil.generateToken(
                user.getPhoneNumber(),
                user.getRole().name()
        );

        // Build response
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(86400000L) // 24 hours in milliseconds
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhoneNumber())
                        .email(user.getEmailAddress())
                        .role(user.getRole())
                        .active(user.getActive())
                        .build())
                .build();
    }
}
