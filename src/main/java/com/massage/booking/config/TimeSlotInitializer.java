package com.massage.booking.config;

import com.massage.booking.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TimeSlotInitializer {

    private final TimeSlotService timeSlotService;

    @Bean
    public CommandLineRunner generateTimeSlots() {
        return args -> {
            // Generate slots for next 3 months on startup
            timeSlotService.generateUpcomingSlots();
            System.out.println("✅ Time slots generated for next 3 months (Thu-Sun only)");
        };
    }
}