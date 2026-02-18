package com.massage.booking.dto.request;

import com.massage.booking.entity.enums.ServiceCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotNull(message = "Category is required")
    private ServiceCategory category;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    private Integer durationMinutes;

    @Min(value = 0, message = "Cleanup time cannot be negative")
    private Integer cleanupMinutes;

    @Size(max = 500)
    private String description;
}