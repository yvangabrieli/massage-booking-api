package com.massage.booking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MassageBookingApiApplication {

	public static void main(String[] args) {
        SpringApplication.run(MassageBookingApiApplication.class, args);
    }
        @Bean
        CommandLineRunner runner (PasswordEncoder encoder){
            return args -> {
                System.out.println("ADMIN HASH:");
                System.out.println(encoder.encode("Admin123!"));
            };
        }

    }
