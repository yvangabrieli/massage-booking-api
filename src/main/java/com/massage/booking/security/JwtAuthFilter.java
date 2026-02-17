package com.massage.booking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Flow:
 * 1. Get token from Authorization header
 * 2. Validate token
 * 3. Load user from database
 * 4. Set user in SecurityContext (marks them as authenticated)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Step 1: Get token from header ──
        String authHeader = request.getHeader("Authorization");

        // If no Authorization header → skip (public endpoint)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        log.debug("JWT token found in request");

        // ── Step 2: Validate token ──
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token in request");
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 3: Extract user info from token ──
        String phone = jwtUtil.extractPhone(token);

        // Only authenticate if not already authenticated
        if (phone != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // ── Step 4: Load user from database ──
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(phone);

            // ── Step 5: Create authentication object ──
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                          // No password needed
                            userDetails.getAuthorities()   // User's roles
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // ── Step 6: Set in SecurityContext ──
            // This marks the user as authenticated for this request
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.debug("User authenticated: {}", phone);
        }

        // Continue to next filter/controller
        filterChain.doFilter(request, response);
    }
}