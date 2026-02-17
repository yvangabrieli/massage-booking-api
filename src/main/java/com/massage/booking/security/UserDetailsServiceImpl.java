package com.massage.booking.security;

import com.massage.booking.entity.valueobject.Phone;
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

/**
 * Spring Security asks: "Given a username, give me the user details"
 * We override this to load users by PHONE (not username)
**/


@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone)
            throws UsernameNotFoundException {

        log.debug("Loading user by phone: {}", phone);

        // Find user in database by phone
        User user = userRepository
                .findByPhone(Phone.of(phone))
                .orElseThrow(() -> {
                    log.warn("User not found with phone: {}", phone);
                    return new UsernameNotFoundException(
                            "User not found with phone: " + phone
                    );
                });

        // Check if user is active
        if (!user.canLogin()) {
            log.warn("Deactivated user tried to login: {}", phone);
            throw new UsernameNotFoundException("User account is deactivated");
        }

        // Convert our User entity to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPasswordHash())
                .authorities(List.of(
                        new SimpleGrantedAuthority(user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(!user.canLogin())
                .credentialsExpired(false)
                .disabled(!user.canLogin())
                .build();
    }
}