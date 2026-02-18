package com.massage.booking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 *
 * @NotBlank  → field cannot be null or empty string
 * @Size      → min/max length
 * @Email     → must be valid email format
 * @Pattern   → must match regex
 *
 * Note: These annotations validate the RAW input from frontend
 * BEFORE it reaches the service layer.
 * Value Objects then validate again when created.
 * Double protection! ✅
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Invalid phone format. Use: +34612345678 or 612345678"
    )
    private String phone;

    @Email(message = "Invalid email format")
    private String email; // Optional - no @NotBlank

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}