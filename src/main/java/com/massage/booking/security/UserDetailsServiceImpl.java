package com.massage.booking.security;

import com.massage.booking.entity.valueobject.Email;
import com.massage.booking.entity.User;
import com.massage.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository
                .findByEmailAndActiveTrue(Email.of(email))
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmailAddress())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().name())))
                .accountExpired(false)
                .accountLocked(!user.canLogin())
                .credentialsExpired(false)
                .disabled(!user.canLogin())
                .build();
    }
}