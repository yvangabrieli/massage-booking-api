package com.massage.booking.dto.response;

import com.massage.booking.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private ClientInfo client;
    private ServiceInfo service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private String guestName;
    private String guestPhone;
    private String canceledReason;
    private Boolean canCancel;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private Long id;
        private String name;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private Long id;
        private String name;
        private Integer durationMinutes;
    }
}