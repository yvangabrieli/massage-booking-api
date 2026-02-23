package com.massage.booking.dto.response;

import com.massage.booking.entity.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private Long id;
    private String name;
    private ServiceCategory category;
    private Integer durationMinutes;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminServiceResponse {
        private Long id;
        private String name;
        private ServiceCategory category;
        private Integer durationMinutes;
        private Integer cleanupMinutes;
        private String description;
        private Boolean active;
        private LocalDateTime createdAt;
    }
}