package com.massage.booking.dto.response;

import com.massage.booking.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 *
 * Returned after both register and login
 * Contains the JWT token + basic user info
 *
 * Frontend stores this token and sends it
 * in every future request:
 * Authorization: Bearer <token>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long expiresIn;   // milliseconds (86400000 = 24h)

    private UserInfo user;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String phone;
        private String email;   // null if not provided
        private Role role;      // ROLE_CLIENT or ROLE_ADMIN
        private Boolean active;
    }
}