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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with phone: {}", request.getPhone());

        Phone phone = Phone.of(request.getPhone());

        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Phone number already registered: " + request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Email email = Email.of(request.getEmail());
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("Email already registered: " + request.getEmail());
            }
        }

        User user = User.createClient(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getPassword(),
                passwordEncoder
        );

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for phone: {}", request.getPhone());

        Phone phone = Phone.of(request.getPhone());

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("Login failed - phone not found: {}", request.getPhone());
                    return new UnauthorizedException("Invalid phone or password");
                });

        if (!user.canLogin()) {
            log.warn("Login failed - user deactivated: {}", request.getPhone());
            throw new UnauthorizedException("Account is deactivated");
        }

        if (!user.checkPassword(request.getPassword(), passwordEncoder)) {
            log.warn("Login failed - wrong password: {}", request.getPhone());
            throw new UnauthorizedException("Invalid phone or password");
        }

        log.info("Login successful for user: {}", user.getId());
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtUtil.getExpiration())
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