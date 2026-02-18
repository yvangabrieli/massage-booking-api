package com.massage.booking.entity;

import com.massage.booking.entity.valueobject.Email;
import com.massage.booking.entity.valueobject.Phone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    private Phone phone;

    @Embedded
    private Email email;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(length = 500)
    private String notes;  // Admin notes (preferences, allergies, etc.)

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "user_id")
    private Long userId;  // Link to User (if client has login account)

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


    public static Client create(
            String name,
            String phoneNumber,
            String emailAddress,
            LocalDate birthday,
            String notes,
            Long userId
    ) {
        Client client = new Client();
        client.setName(name);
        client.setPhone(Phone.of(phoneNumber));
        client.setEmail(Email.of(emailAddress));  // null if blank
        client.setBirthday(birthday);
        client.setNotes(notes);
        client.setUserId(userId);
        client.setActive(true);
        return client;
    }


    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Client already deactivated");
        }
        this.active = false;
    }

    public void activate() {
        if (this.active) {
            throw new IllegalStateException("Client already active");
        }
        this.active = true;
    }

    public void updateProfile(
            String name,
            String emailAddress,
            LocalDate birthday,
            String notes
    ) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.email = Email.of(emailAddress);
        this.birthday = birthday;
        this.notes = notes;
    }

    // Helper methods for API
    public String getPhoneNumber() {
        return phone != null ? phone.getValue() : null;
    }

    public String getEmailAddress() {
        return email != null ? email.getValue() : null;
    }

    public boolean hasUser() {
        return userId != null;
    }
}