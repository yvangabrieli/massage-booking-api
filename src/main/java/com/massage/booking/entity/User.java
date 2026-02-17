package com.massage.booking.entity;

import com.massage.booking.entity.enums.Role;
import com.massage.booking.entity.valueobject.Email;
import com.massage.booking.entity.valueobject.Password;
import com.massage.booking.entity.valueobject.Phone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Only factory methods can create
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    private Phone phone;

    @Embedded
    private Email email;

    @Column(nullable = false, name = "password")  // Store hashed password
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_CLIENT;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ✅ DDD: Factory method to create User
    public static User createClient(
            String name,
            String phoneNumber,
            String emailAddress,
            String rawPassword,
            PasswordEncoder encoder
    ) {
        // ✅ Value Objects validate automatically!
        Phone phone = Phone.of(phoneNumber);
        Email email = Email.of(emailAddress);  // Can be null (optional)
        Password password = Password.fromRaw(rawPassword, encoder);

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(password.getHashedValue());
        user.setRole(Role.ROLE_CLIENT);
        user.setActive(true);

        return user;
    }

    public static User createAdmin(
            String name,
            String phoneNumber,
            String emailAddress,
            String rawPassword,
            PasswordEncoder encoder
    ) {
        User user = createClient(name, phoneNumber, emailAddress, rawPassword, encoder);
        user.setRole(Role.ROLE_ADMIN);
        return user;
    }

    // ✅ DDD: Business logic methods

    /**
     * Check if password matches
     */
    public boolean checkPassword(String rawPassword, PasswordEncoder encoder) {
        Password password = Password.fromHashed(this.passwordHash);
        return password.matches(rawPassword, encoder);
    }

    /**
     * Change password
     */
    public void changePassword(String newRawPassword, PasswordEncoder encoder) {
        Password newPassword = Password.fromRaw(newRawPassword, encoder);
        this.passwordHash = newPassword.getHashedValue();
    }

    /**
     * Business rule: Check if user can login
     */
    public boolean canLogin() {
        return this.active;
    }

    /**
     * Business rule: Deactivate user
     */
    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.active = false;
    }

    /**
     * Business rule: Activate user
     */
    public void activate() {
        if (this.active) {
            throw new IllegalStateException("User is already active");
        }
        this.active = true;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return this.role == Role.ROLE_ADMIN;
    }

    /**
     * Check if user is client
     */
    public boolean isClient() {
        return this.role == Role.ROLE_CLIENT;
    }

    /**
     * Get phone number as string (for responses)
     */
    public String getPhoneNumber() {
        return phone != null ? phone.getValue() : null;
    }

    /**
     * Get email as string (for responses)
     */
    public String getEmailAddress() {
        return email != null ? email.getValue() : null;
    }
}