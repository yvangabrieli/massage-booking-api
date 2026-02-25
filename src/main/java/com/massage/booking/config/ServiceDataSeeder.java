package com.massage.booking.config;

import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.enums.ServiceCategory;
import com.massage.booking.repository.MassageServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ServiceDataSeeder implements CommandLineRunner {

    private final MassageServiceRepository serviceRepository;

    public ServiceDataSeeder(MassageServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if services already exist
        if (serviceRepository.count() > 0) {
            System.out.println("Services already seeded. Skipping...");
            return;
        }

        List<MassageService> services = List.of(
                MassageService.create(
                        "Toque Profundo 60",
                        ServiceCategory.DEEP_TISSUE,
                        60,
                        10,
                        BigDecimal.valueOf(65.00),
                        "Deep tissue massage 60 minutes"
                ),
                MassageService.create(
                        "Toque Profundo 90",
                        ServiceCategory.DEEP_TISSUE,
                        90,
                        10,
                        BigDecimal.valueOf(85.00),
                        "Deep tissue massage 90 minutes"
                ),
                MassageService.create(
                        "Relax Premium",
                        ServiceCategory.RELAXING,
                        75,
                        10,
                        BigDecimal.valueOf(70.00),
                        "Premium relaxation massage"
                ),
                MassageService.create(
                        "Siesta Express",
                        ServiceCategory.RELAXING,
                        40,
                        10,
                        BigDecimal.valueOf(43.00),
                        "Quick relaxation session"
                ),
                MassageService.create(
                        "Happy Feet",
                        ServiceCategory.SPECIALIZED,
                        30,
                        10,
                        BigDecimal.valueOf(25.00),
                        "Foot reflexology massage"
                ),
                MassageService.create(
                        "Libera Mi Espalda",
                        ServiceCategory.SPECIALIZED,
                        45,
                        10,
                        BigDecimal.valueOf(50.00),
                        "Back pain relief massage"
                )
        );

        serviceRepository.saveAll(services);
        System.out.println("Seeded " + services.size() + " services.");
    }
}