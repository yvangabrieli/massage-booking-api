package com.massage.booking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Massage Booking API",
                version = "1.0.0",
                description = """
            REST API for managing massage center bookings, clients, and services.
            
            ## Features
            - JWT-based authentication
            - Role-based access control (ADMIN, CLIENT)
            - Client management
            - Service catalog management
            - Booking system with business rules
            - Automated notifications
            
            ## Authentication
            Most endpoints require authentication via JWT token.
            1. Register a new account using `/v1/auth/register`
            2. Login using `/v1/auth/login` to receive a JWT token
            3. Include the token in the Authorization header: `Bearer <token>`
            
            ## Roles
            - **ADMIN**: Full access to all resources
            - **CLIENT**: Limited access to own resources only
            
            ## Business Rules
            - Bookings must be made at least **2 hours in advance**
            - Clients can cancel only if **≥12 hours before** appointment
            - Each service includes **10 minutes cleanup time** (hidden from clients)
            - Admins can override time rules
            """,
                contact = @Contact(
                        name = "Backend Development Team",
                        email = "Tokamemassage@gmail.com",
                        url = "https://github.com/yvangabrieli/massage-booking-api"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(description = "Local Development Server", url = "http://localhost:8080/api"),
                @Server(description = "Production Server", url = "https://api.massagebooking.com/api")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        description = "JWT authentication token. Obtain by logging in via /v1/auth/login",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}