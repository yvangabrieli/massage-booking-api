# 💆 Massage Booking API
### *API de Reservas de Masajes — Java Spring Boot con Seguridad JWT*

> A production-minded REST API for managing massage appointments, built as the **Sprint 5 final project** of the IT Academy Java with Spring Framework programme. Features layered architecture, JWT security, Value Objects, async email notifications, and role-based access control.
>
> **Backend: complete and tested. Frontend: a new environment actively being explored — not all endpoints are wired yet. Solid foundation, growing forward.**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Repositories](#-repositories)
- [Architecture](#-architecture--layered-n-tier)
- [Project Structure](#-project-structure)
- [Layer Breakdown](#-layer-breakdown)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [API Endpoints](#-api-endpoints)
- [Getting Started](#-getting-started)
- [Security](#-security)
- [Testing](#-testing)
- [Logging](#-logging)
- [AI-Assisted Frontend](#-ai-assisted-frontend)
- [Project Reflection](#-project-reflection)
- [Author](#-author)

---

## 🌟 Project Overview
### *Descripción del Proyecto*

The **Massage Booking API** is a RESTful web service that allows clients to register, log in, browse massage services, check availability, and manage their appointments. Admins have full system access.

This project was built as the **Sprint 5 capstone** of IT Academy's Java with Spring Framework specialisation. Rather than the suggested "Virtual Pet" domain, a **Massage Booking** domain was chosen as a real-world, practical alternative — demonstrating the same core principles: authentication, CRUD, role-based access, and AI-assisted frontend development.

The architecture is **Classic N-Layer (Layered)** — the standard, battle-tested Spring MVC pattern — enriched with **Value Objects** from Domain-Driven Design applied within the entity layer.

---

## 🗂 Repositories
### *Repositorios*

| Layer | Repository | Status |
|-------|-----------|--------|
| 🔧 Backend (Spring Boot) | [massage-booking-api](https://github.com/yvangabrieli/massage-booking-api) | ✅ Complete |
| 🎨 Frontend (AI-assisted) | [massage-booking-front](https://github.com/yvangabrieli/massage-booking-front) | 🚧 In Progress |

---

## 🏛 Architecture — Layered (N-Tier)
### *Arquitectura por Capas — Patrón Clásico Spring MVC*

This project uses **Classic Layered Architecture** — not Hexagonal. Each layer has a single responsibility and communicates only with the layer directly below it. This is the standard pattern for the vast majority of production Spring Boot applications.

```
┌─────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                          │
│              controller/  ·  dto/request  ·  dto/response       │
│           (HTTP IN — receives requests, returns responses)       │
└──────────────────────────────┬──────────────────────────────────┘
                               │ delegates to
┌──────────────────────────────▼──────────────────────────────────┐
│                      BUSINESS LAYER                             │
│                          service/                               │
│          (business rules, orchestration, @Async email)          │
└──────────────────────────────┬──────────────────────────────────┘
                               │ persists via
┌──────────────────────────────▼──────────────────────────────────┐
│                    DATA ACCESS LAYER                            │
│                        repository/                              │
│              (Spring Data JPA — extends JpaRepository)          │
└──────────────────────────────┬──────────────────────────────────┘
                               │ maps to
┌──────────────────────────────▼──────────────────────────────────┐
│                      DOMAIN LAYER                               │
│              entity/  ·  entity/valueobject/  ·  entity/enums/  │
│          (JPA entities enriched with DDD Value Objects)         │
└─────────────────────────────────────────────────────────────────┘

                    ─── CROSS-CUTTING ───
          security/  ·  exception/  ·  config/  ·  logs/
```

> **Note on Value Objects:** Although the architecture is Layered, the `entity/valueobject/` package applies a DDD concept — `Email`, `Password`, `Phone`, and `WorkingDay` are immutable objects that validate themselves on construction. This enriches the domain model without requiring a full Hexagonal structure.

---

## 📁 Project Structure
### *Estructura del Proyecto*

```
massage-booking-api/
│
├── .mvn/                                    ← Maven wrapper config
├── logs/
│   └── massage-booking-api.log              ← Application log output
│
├── src/
│   ├── main/
│   │   ├── java/com/massage/booking/
│   │   │   │
│   │   │   ├── config/                      ← Cross-cutting configuration
│   │   │   │   ├── AsyncConfig.java             @EnableAsync + thread pool
│   │   │   │   ├── OpenApiConfig.java            Swagger / OpenAPI 3 setup
│   │   │   │   ├── ServiceDataSeeder.java        Seeds massage catalogue on startup
│   │   │   │   └── TimeSlotInitializer.java      Generates available time slots
│   │   │   │
│   │   │   ├── controller/                  ← Presentation Layer (HTTP IN)
│   │   │   │   ├── AdminController.java         Admin-only endpoints
│   │   │   │   ├── AuthController.java           /register + /login
│   │   │   │   ├── AvailabilityController.java   GET available time slots
│   │   │   │   ├── BookingController.java         CRUD bookings
│   │   │   │   ├── ClientController.java          Client profile management
│   │   │   │   └── ServiceController.java         Massage service catalogue
│   │   │   │
│   │   │   ├── dto/                         ← API Contract (decouples HTTP from DB)
│   │   │   │   ├── request/
│   │   │   │   │   ├── BookingRequest.java
│   │   │   │   │   ├── ClientRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── ServiceRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java        Carries JWT token on login
│   │   │   │       ├── BookingResponse.java
│   │   │   │       ├── ClientResponse.java
│   │   │   │       └── ServiceResponse.java
│   │   │   │
│   │   │   ├── entity/                      ← Domain / Persistence Layer
│   │   │   │   ├── enums/
│   │   │   │   │   ├── BookingStatus.java       PENDING · CONFIRMED · CANCELLED · COMPLETED
│   │   │   │   │   ├── Role.java                 ROLE_USER · ROLE_ADMIN
│   │   │   │   │   └── ServiceCategory.java      RELAXATION · THERAPEUTIC · SPORT…
│   │   │   │   ├── valueobject/             ← DDD Value Objects (Rich Model)
│   │   │   │   │   ├── Email.java               Validates format on construction
│   │   │   │   │   ├── Password.java             Enforces strength rules
│   │   │   │   │   ├── Phone.java                Normalises phone format
│   │   │   │   │   └── WorkingDay.java           Encapsulates schedule logic
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Client.java
│   │   │   │   ├── MassageService.java
│   │   │   │   ├── TimeSlot.java
│   │   │   │   └── User.java
│   │   │   │
│   │   │   ├── exception/                   ← Error Handling Strategy
│   │   │   │   ├── BusinessException.java        Base custom exception → 400
│   │   │   │   ├── DuplicateResourceException.java → 409 Conflict
│   │   │   │   ├── GlobalExceptionHandler.java   @RestControllerAdvice
│   │   │   │   ├── ResourceNotFoundException.java → 404 Not Found
│   │   │   │   └── UnauthorizedException.java    → 401 Unauthorized
│   │   │   │
│   │   │   ├── repository/                  ← Data Access Layer (Spring Data JPA)
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── MassageServiceRepository.java
│   │   │   │   ├── TimeSlotRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── WorkingDayRepository.java
│   │   │   │
│   │   │   ├── security/                    ← Security Infrastructure
│   │   │   │   ├── JwtAuthFilter.java           OncePerRequestFilter — validates Bearer
│   │   │   │   ├── JwtUtil.java                  Generate / validate / extract claims
│   │   │   │   ├── SecurityConfig.java            FilterChain, CORS, stateless sessions
│   │   │   │   └── UserDetailsServiceImpl.java    Loads User from DB for Spring Security
│   │   │   │
│   │   │   ├── service/                     ← Business Logic Layer
│   │   │   │   ├── AuthService.java              Register + Login + JWT issue
│   │   │   │   ├── BookingService.java            Core booking orchestration
│   │   │   │   ├── ClientService.java             Client profile CRUD
│   │   │   │   ├── EmailNotificationService.java  @Async email on booking events
│   │   │   │   ├── ServiceCatalogService.java     Manage massage service catalogue
│   │   │   │   └── TimeSlotService.java           Slot availability logic
│   │   │   │
│   │   │   └── MassageBookingApiApplication.java  ← Entry point
│   │   │
│   │   └── resources/
│   │       ├── application.yml              ← Externalised configuration
│   │       ├── data.sql                     ← Seed data on startup
│   │       └── database-schema.sql          ← DDL schema definition
│   │
│   └── test/
│       ├── java/com/massage/booking/
│       │   ├── entity/valueobject/
│       │   │   ├── EmailTest.java           ← Validates Email VO invariants
│       │   │   ├── PasswordTest.java         ← Validates Password VO strength rules
│       │   │   └── PhoneTest.java            ← Validates Phone VO format
│       │   ├── security/
│       │   │   └── JwtUtilTest.java          ← Token generation, expiry, claims
│       │   ├── service/
│       │   │   ├── AuthServiceTest.java      ← Register/login business logic
│       │   │   └── BookingServiceTest.java   ← Booking rules with mocked repos
│       │   └── MassageBookingApiApplicationTests.java  ← Context loads
│       └── resources/
│
├── .gitattributes
├── .gitignore
├── mvnw / mvnw.cmd                          ← Maven wrapper scripts
└── pom.xml                                  ← Dependencies and build config
```

---

## 🗃 Layer Breakdown
### *Descripción de Capas — Conexión con el Curriculum del IT Academy*

### 1. 🌐 Controller Layer — Presentation
*Capa de Presentación / Entrada HTTP*

Receives HTTP requests, validates input shape (via DTOs), delegates all logic to the service layer, and returns HTTP responses. Controllers **never contain business logic**.

| File | Responsibility | Role Required |
|------|---------------|---------------|
| `AuthController` | `/register` + `/login` | Public |
| `BookingController` | CRUD for appointments | JWT |
| `AvailabilityController` | GET available time slots | JWT |
| `ServiceController` | Browse massage catalogue | JWT |
| `ClientController` | Client profile management | JWT |
| `AdminController` | System-wide admin operations | ADMIN |

> 📚 **Sprint 4** — REST API with Spring Boot (`@RestController`, `@RequestMapping`, `@PostMapping`…)

---

### 2. ⚙️ Service Layer — Business Logic
*Capa de Lógica de Negocio*

All business rules live here. Services are Spring-managed beans (`@Service`) injected via constructor. The `EmailNotificationService` is decorated with `@Async` — booking confirmations are dispatched on a separate thread so the HTTP response returns immediately.

| File | Responsibility |
|------|---------------|
| `AuthService` | Credential validation, JWT issuance |
| `BookingService` | Booking creation, ownership checks, status transitions |
| `ClientService` | Profile CRUD with validation |
| `EmailNotificationService` | `@Async` email dispatch on booking events |
| `ServiceCatalogService` | Massage type management |
| `TimeSlotService` | Availability calculation logic |

> 📚 **Sprint 4** — Spring IoC & Dependency Injection (`@Service`, `@Autowired`, constructor injection)
> 📚 **Sprint 5** — `@Async` + `AsyncConfig` thread pool configuration

---

### 3. 🗄 Repository Layer — Data Access
*Capa de Acceso a Datos*

Spring Data JPA repositories. Each interface extends `JpaRepository<Entity, Long>` and gets full CRUD for free. Custom queries are defined via method naming conventions or `@Query` annotations.

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByClientId(Long clientId);
    List<Booking> findByStatus(BookingStatus status);
}
```

> 📚 **Sprint 2** — MySQL + JPA (`@Entity`, `@Repository`, Spring Data query methods)

---

### 4. 🏗 Entity Layer — Domain Model + Value Objects
*Capa de Dominio — Modelo Rico con Objetos de Valor*

JPA entities representing the core business concepts, enriched with **DDD Value Objects** in the `valueobject/` sub-package.

**Entities:** `Booking`, `Client`, `MassageService`, `TimeSlot`, `User`

**Enums:**
- `BookingStatus` — `PENDING` · `CONFIRMED` · `CANCELLED` · `COMPLETED`
- `Role` — `ROLE_USER` · `ROLE_ADMIN`
- `ServiceCategory` — `RELAXATION` · `THERAPEUTIC` · `SPORT`…

**Value Objects** (`@Embeddable` — validated on construction, equality by value):

| Class | Validates |
|-------|-----------|
| `Email` | Format: `user@domain.tld` — lowercase and trimmed |
| `Password` | Strength rules — minimum length, complexity |
| `Phone` | International format normalisation |
| `WorkingDay` | Schedule/hours business logic |

```java
// If you have an Email object, it is already valid. Invalid state is impossible.
@Embeddable
public class Email {
    private String value;
    public Email(String value) {
        if (value == null || !value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("Invalid email format: " + value);
        }
        this.value = value.toLowerCase().trim();
    }
}
```

> 📚 **Sprint 1** — Java OOP, Enums, Annotations
> 📚 **Sprint 3** — Design Patterns (Value Object is a DDD pattern applied within the layered architecture)

---

### 5. 🔒 Security — Cross-Cutting
*Seguridad Transversal*

Spring Security configured for **stateless JWT authentication**. The `JwtAuthFilter` intercepts every request before it reaches a controller, validates the Bearer token, and populates the `SecurityContext`.

```
POST /api/auth/login
  → AuthService validates credentials (BCrypt)
  → JwtUtil generates HMAC-SHA256 signed token
  → AuthResponse returns token to client
  → Client attaches token as: Authorization: Bearer <token>
  → JwtAuthFilter validates on every subsequent request
  → SecurityContext populated with username + roles
  → SecurityConfig rules + @PreAuthorize enforce access
```

> 📚 **Sprint 5** — Spring Security, JWT, stateless sessions, filter chains

---

### 6. 🚨 Exception Strategy — Cross-Cutting
*Estrategia de Manejo de Errores*

A custom exception hierarchy mapped to HTTP status codes by a single `@RestControllerAdvice` class. Controllers throw; the handler translates.

| Exception | HTTP Status |
|-----------|-------------|
| `BusinessException` | 400 Bad Request |
| `ResourceNotFoundException` | 404 Not Found |
| `DuplicateResourceException` | 409 Conflict |
| `UnauthorizedException` | 401 Unauthorized |

> 📚 **Sprint 1** — Exceptions & error handling in Java

---

## 🛠 Tech Stack
### *Tecnologías Utilizadas*

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Primary language |
| Spring Boot | 3.x | Application framework |
| Spring Security | 6.x | Authentication & authorisation |
| Spring Data JPA | 3.x | ORM and data access layer |
| Hibernate | 6.x | JPA provider / ORM |
| JWT (JJWT) | 0.11+ | Stateless token generation & validation |
| MySQL | 8.0 | Relational database |
| Springdoc OpenAPI | 2.x | Swagger UI + OpenAPI 3 docs |
| SLF4J + Logback | — | Structured logging |
| JUnit 5 | — | Test framework |
| Mockito | — | Mocking for unit tests |
| Maven | 3.8+ | Build & dependency management |

**Frontend (AI-assisted):**

| Technology | Purpose |
|-----------|---------|
| HTML / CSS / JavaScript | UI structure and styling |
| Fetch API | HTTP communication with the backend |
| Claude (Anthropic) | Primary AI — context-aware code generation |
| ChatGPT (OpenAI) | Boilerplate, conceptual explanations |
| v0 (Vercel) | React/Tailwind visual component generation |
| Kimi (Moonshot AI) | Long-context documentation reading |

---

## ✨ Features
### *Funcionalidades*

**Authentication & Users**
- ✅ Register a new account (username, email, password)
- ✅ Login and receive a signed JWT token
- ✅ Role-based access control: `ROLE_USER` and `ROLE_ADMIN`

**Bookings**
- ✅ Create a new massage appointment
- ✅ Read own bookings (USER) or all bookings (ADMIN)
- ✅ Update booking status / details
- ✅ Cancel and delete a booking
- ✅ Async email confirmation on booking creation

**Availability**
- ✅ Browse available time slots
- ✅ Time slots seeded and managed by `TimeSlotInitializer`

**Massage Services**
- ✅ Browse service catalogue (all authenticated users)
- ✅ Create / Update / Delete services (ADMIN only)
- ✅ Seeded on startup via `ServiceDataSeeder`

**Client Profiles**
- ✅ View and update own profile
- ✅ Admin: view all clients

**Role Access Matrix:**

| Endpoint group | `ROLE_USER` | `ROLE_ADMIN` |
|---------------|------------|-------------|
| `POST /api/auth/**` | Public | Public |
| `GET /api/bookings` | Own only | All bookings |
| `POST /api/bookings` | ✅ | ✅ |
| `PUT/DELETE /api/bookings/{id}` | Own only | Any |
| `GET /api/services` | ✅ Browse | ✅ |
| `POST/PUT/DELETE /api/services` | ❌ | ✅ |
| `GET /api/admin/**` | ❌ | ✅ |

---

## 📡 API Endpoints
### *Endpoints de la API*

> 🔎 **Interactive documentation available at:** `http://localhost:8080/swagger-ui.html`
> All endpoints below are implemented in the backend. Frontend integration is ongoing — some endpoints not yet wired in the UI.

### 🔐 Authentication
```
POST   /api/auth/register        Register new user, receive JWT
POST   /api/auth/login           Login with credentials, receive JWT
```

### 📅 Bookings
```
GET    /api/bookings             List bookings (own for USER, all for ADMIN)
GET    /api/bookings/{id}        Get a specific booking
POST   /api/bookings             Create new booking (triggers async email)
PUT    /api/bookings/{id}        Update booking status or details  [⚠ partial FE]
DELETE /api/bookings/{id}        Cancel and delete booking         [⚠ partial FE]
```

### 🕐 Availability
```
GET    /api/availability         Get available time slots
GET    /api/availability/{date}  Slots for a specific date          [⚠ partial FE]
```

### 💆 Services
```
GET    /api/services             List all massage types
GET    /api/services/{id}        Get specific service
POST   /api/services             Create service (ADMIN)
PUT    /api/services/{id}        Update service (ADMIN)             [⚠ partial FE]
DELETE /api/services/{id}        Remove service (ADMIN)             [⚠ partial FE]
```

### 👤 Clients
```
GET    /api/clients/me           Get own client profile
PUT    /api/clients/me           Update own profile                 [⚠ partial FE]
```

### 🛡 Admin
```
GET    /api/admin/bookings       View all system bookings (ADMIN)
GET    /api/admin/clients        View all registered clients (ADMIN)[⚠ partial FE]
PUT    /api/admin/bookings/{id}  Admin update any booking (ADMIN)   [⚠ partial FE]
```

### 📚 Documentation
```
GET    /swagger-ui.html          Interactive Swagger UI
GET    /v3/api-docs              OpenAPI 3.0 JSON specification
```

> `[⚠ partial FE]` = endpoint fully implemented in backend, frontend integration in progress.

---

## 🚀 Getting Started
### *Puesta en Marcha*

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8+
- Git

### 1. Clone the repository

```bash
git clone https://github.com/yvangabrieli/massage-booking-api.git
cd massage-booking-api
```

### 2. Create the database

```sql
CREATE DATABASE massage_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure `application.yml`

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/massage_booking_db
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

app:
  jwt:
    secret: your-very-secure-256-bit-secret-key-here
    expiration: 86400000   # 24 hours in ms

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

> ⚠️ On first run, `ServiceDataSeeder` and `TimeSlotInitializer` populate the database automatically from `data.sql`.

### 4. Build and run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API starts at **`http://localhost:8080`**
Swagger UI at **`http://localhost:8080/swagger-ui.html`**

### 5. Quick test with cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"SecurePass1!"}'

# Login — copy the token from the response
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass1!"}'

# Use token
curl -X GET http://localhost:8080/api/bookings \
  -H "Authorization: Bearer <your_jwt_token>"
```

---

## 🔒 Security
### *Seguridad — JWT Stateless Authentication*

### JWT Authentication Flow

```
1.  Client  →  POST /api/auth/login  {email, password}
2.  AuthService validates credentials against DB (BCrypt comparison)
3.  JwtUtil generates HMAC-SHA256 signed token with username + role claims
4.  Response  →  { "token": "eyJhbGci..." }
5.  Client stores token and sends:  Authorization: Bearer <token>
6.  JwtAuthFilter (OncePerRequestFilter) intercepts every request
7.  Token signature and expiry validated by JwtUtil
8.  SecurityContextHolder populated with authenticated user + authorities
9.  SecurityConfig rules + @PreAuthorize enforce role-based access
```

### SecurityConfig highlights

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)                    // safe for stateless REST
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .cors(Customizer.withDefaults())                          // CORS before auth filters
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

### CORS Configuration

```java
@Configuration
public class SecurityConfig {
    // CORS must be configured before Spring Security processes requests.
    // Authorization header must be explicitly listed in allowedHeaders.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:5500"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

---

## 🧪 Testing
### *Pruebas — Estrategia de Tests*

```bash
./mvnw test
```

### Test structure (actual files)

```
src/test/java/com/massage/booking/
│
├── entity/valueobject/
│   ├── EmailTest.java          ← Value Object invariants: valid formats pass, invalid throw
│   ├── PasswordTest.java       ← Strength rules enforced on construction
│   └── PhoneTest.java          ← Format normalisation validated
│
├── security/
│   └── JwtUtilTest.java        ← Token generation, signature, expiry, claim extraction
│
├── service/
│   ├── AuthServiceTest.java    ← Register/login logic with mocked UserRepository + JwtUtil
│   └── BookingServiceTest.java ← Booking rules, ownership, async email trigger
│
└── MassageBookingApiApplicationTests.java  ← Spring context loads successfully
```

### Testing approach

**Value Object tests** — pure Java, no Spring context needed. The fastest and most important tests: if `Email` allows an invalid address, the entire domain is unsound.

```java
class EmailTest {

    @Test
    void validEmail_createsSuccessfully() {
        Email email = new Email("user@example.com");
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void invalidEmail_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> new Email("not-an-email"));
    }

    @Test
    void email_isNormalisedToLowercase() {
        Email email = new Email("USER@EXAMPLE.COM");
        assertEquals("user@example.com", email.getValue());
    }
}
```

**Service tests** — Mockito mocks the repositories; tests focus purely on business logic.

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepo;
    @Mock EmailNotificationService emailService;
    @InjectMocks BookingService bookingService;

    @Test
    void createBooking_persistsAndTriggersEmail() {
        when(bookingRepo.save(any())).thenReturn(mockBooking());
        bookingService.createBooking(mockRequest(), "user@test.com");
        verify(emailService).sendBookingConfirmation(any());
    }
}
```

---

## 📊 Logging
### *Registro de Actividad*

Application logs are written to `logs/massage-booking-api.log` (configured in `application.yml` via Logback).

```java
@Slf4j
@Service
public class BookingService {

    public BookingResponse createBooking(BookingRequest req, String username) {
        log.info("Creating booking for user: {}", username);
        try {
            Booking saved = bookingRepo.save(booking);
            log.info("Booking created successfully. ID: {}", saved.getId());
            emailService.sendBookingConfirmation(saved); // async
            return BookingResponse.from(saved);
        } catch (Exception e) {
            log.error("Booking creation failed for user {}: {}", username, e.getMessage());
            throw e;
        }
    }
}
```

| Level | Used for |
|-------|---------|
| `DEBUG` | Detailed flow during development |
| `INFO` | Key operations: booking created, user registered, slot initialised |
| `WARN` | Unexpected but recoverable states (e.g. invalid JWT attempt) |
| `ERROR` | Failures that need attention |

---

## 🤖 AI-Assisted Frontend
### *Desarrollo Frontend Asistido por IA*

The frontend was built using **multiple AI tools in combination**, each serving a different purpose.

| Tool | Provider | Primary Use |
|------|---------|------------|
| **Claude** | Anthropic | Main tool — context-aware Spring Boot integration, CORS diagnosis, JWT flow |
| **ChatGPT** | OpenAI | Boilerplate, conceptual explanations, second opinions |
| **v0** | Vercel | React/Tailwind visual component generation with instant preview |
| **Kimi** | Moonshot AI | Long-context reading of docs, full pom.xml / yml analysis |

### Key interactions

| Prompt | AI | Outcome |
|--------|-----|---------|
| *"Generate a login form that POSTs to /api/auth/login and stores the JWT"* | Claude | Fetch-based form with localStorage token handling |
| *"My requests return 401 even with a valid token"* | Claude | Identified missing `addFilterBefore` in SecurityConfig |
| *"Getting CORS errors from the frontend"* | Claude | Identified Authorization header missing from allowedHeaders, CORS must run before Security filters |
| *"Create a booking dashboard with status badges"* | v0 | React component with Tailwind, required adaptation for real API |
| *"Explain the Spring Security filter chain"* | ChatGPT | Clear conceptual walkthrough |
| *"What Swagger config is missing for it to work with Security?"* | Kimi | Correctly identified missing permit in SecurityConfig |

### What AI-generated code needed — developer adjustments

- Replaced all placeholder URLs with real endpoint paths
- Removed hardcoded mock data — replaced with live Fetch calls
- Added proper JWT token persistence and expiry handling
- Fixed CORS by adding `Authorization` to `allowedHeaders`
- Added error state handling and loading indicators
- Validated role logic against actual JWT claims structure

> **Key insight:** AI accelerates the scaffold. The developer's understanding of the architecture determines whether it actually works. Every generated line was read, understood, and validated before use.

### Running the frontend

```bash
git clone https://github.com/yvangabrieli/massage-booking-front.git
cd massage-booking-front
# Open index.html in a browser or use VS Code Live Server
```

---

## 💭 Project Reflection
### *Reflexión sobre el Proceso de Aprendizaje*

This project brought together every Sprint of the IT Academy curriculum into one cohesive application:

**Sprint 0 → Sprint 5 — what each left in the codebase:**

| Sprint | Concepts | Where in project |
|--------|---------|-----------------|
| Sprint 0 | Git, Maven, clean code | `.gitignore`, `pom.xml`, commit history |
| Sprint 1 | OOP, Exceptions, Enums, Lambdas, Annotations | `entity/`, `exception/`, `enums/`, stream operations |
| Sprint 2 | MySQL, JPA, queries | `repository/`, `data.sql`, `database-schema.sql` |
| Sprint 3 | Design Patterns, DDD concepts | `entity/valueobject/` — Value Objects |
| Sprint 4 | Spring IoC, REST API, DTOs | `controller/`, `service/`, `dto/` |
| Sprint 5 | JWT Security, @Async, OpenAPI, AI frontend | `security/`, `AsyncConfig`, `OpenApiConfig` |

**Challenges overcome:**
- Spring Security filter chain ordering — CORS must be configured before the authentication filters; Swagger paths must be explicitly permitted
- CORS preflight (OPTIONS) request — the browser sends it before every authenticated request; understanding it was the key to unblocking the frontend
- Value Objects in JPA — `@Embeddable` + `@Embedded` pattern took iteration to get right
- AI-generated code required critical review — architecturally incorrect code that was syntactically valid appeared multiple times

> *"The best engineers are not those who know everything — they are those who know how to learn everything."*

---

## 👨‍💻 Author
### *Autor*

**Yvan Gabrieli**
IT Academy Barcelona — Java amb Spring Framework Graduate (2025)

- 🐙 GitHub: [@yvangabrieli](https://github.com/yvangabrieli)
- 🔧 Backend: [massage-booking-api](https://github.com/yvangabrieli/massage-booking-api)
- 🎨 Frontend: [massage-booking-front](https://github.com/yvangabrieli/massage-booking-front)

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

*Built with ☕ Java, 🍃 Spring Boot, 🔐 JWT, and a lot of learning — IT Academy Barcelona 2025*