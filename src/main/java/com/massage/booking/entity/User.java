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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(nullable = false, name = "password")
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

    public static User createClient(
            String name,
            String phoneNumber,
            String emailAddress,
            String rawPassword,
            PasswordEncoder encoder
    ) {
        Phone phone = Phone.of(phoneNumber);
        Email email = Email.of(emailAddress);
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

    public void promoteToSubAdmin() {
        if (this.role == Role.ROLE_ADMIN) {
            throw new IllegalStateException("User is already an admin");
        }
        this.role = Role.ROLE_SUBADMIN;
    }

    public void demoteToClient() {
        if (this.role == Role.ROLE_CLIENT) {
            throw new IllegalStateException("User is already a client");
        }
        if (this.role == Role.ROLE_ADMIN) {
            throw new IllegalStateException("Cannot demote an admin");
        }
        this.role = Role.ROLE_CLIENT;
    }

    public boolean checkPassword(String rawPassword, PasswordEncoder encoder) {
        Password password = Password.fromHashed(this.passwordHash);
        return password.matches(rawPassword, encoder);
    }

    public void changePassword(String newRawPassword, PasswordEncoder encoder) {
        Password newPassword = Password.fromRaw(newRawPassword, encoder);
        this.passwordHash = newPassword.getHashedValue();
    }

    public boolean canLogin() {
        return this.active;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("User is already deactivated");
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("User is already active");
        }
        this.active = true;
    }

    public boolean isAdmin() {
        return this.role == Role.ROLE_ADMIN;
    }

    public boolean isSubAdmin() {
        return this.role == Role.ROLE_SUBADMIN;
    }

    public boolean isClient() {
        return this.role == Role.ROLE_CLIENT;
    }

    public String getPhoneNumber() {
        return phone != null ? phone.getValue() : null;
    }

    public String getEmailAddress() {
        return email != null ? email.getValue() : null;
    }
}