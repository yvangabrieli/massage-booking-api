package com.massage.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private LocalDate birthday;
    private String notes;
    private Boolean active;
    private Long userId;
    private LocalDateTime createdAt;
}