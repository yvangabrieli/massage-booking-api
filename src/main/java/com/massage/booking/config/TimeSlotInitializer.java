package com.massage.booking.config;

import com.massage.booking.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TimeSlotInitializer {

    private final TimeSlotService timeSlotService;

    @Bean
    public CommandLineRunner generateTimeSlots() {
        return args -> {
            timeSlotService.generateUpcomingSlots();
            log.info("Time slots generated for next 3 months (Thu-Sun only)");
        };
    }
}