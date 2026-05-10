# Step 1 — Project Overview & Tech Stack

> **Prompt**: "Give me a high-level overview of this Spring Boot portfolio backend. What does it do, what tech does it use, and how is the source code organized by package?"

---

## What This App Is

This is the **REST API backend** for Ajay Nair's developer portfolio website. The frontend (Next.js, hosted on Vercel) calls this backend to fetch all portfolio content — profile info, projects, skills, experience — and to let visitors send contact messages. There is also an **admin interface** for updating all that content, protected by JWT authentication.

It is hosted on **Render** (backend) and uses **NeonDB** (serverless PostgreSQL) as the database.

---

## Entry Point — `PortfolioApplication.java`

```java
@SpringBootApplication
@EnableAsync
public class PortfolioApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}
```

- `@SpringBootApplication` = `@Configuration` + `@ComponentScan` + `@EnableAutoConfiguration` all in one. It bootstraps the whole app.
- `@EnableAsync` enables background task execution. This is used by `ContactServiceImpl.sendDiscordNotification()` so Discord alerts don't block the HTTP response.
- `SpringApplication.run(...)` starts the embedded Tomcat server and initializes the Spring context.

---

## Tech Stack (from `build.gradle`)

| Dependency | Version | Why It's Here |
|---|---|---|
| **Spring Boot** | 4.0.3 | Core framework |
| **Java** | 25 | Language version |
| **Spring Web MVC** | (starter) | REST controllers, HTTP handling |
| **Spring Data JPA** | (starter) | Database access via repository interfaces |
| **Spring Security** | (starter) | JWT auth, route protection |
| **Spring Validation** | (starter) | `@Valid`, `@NotBlank`, `@Email` on request DTOs |
| **Spring Actuator** | (starter) | `/actuator/health`, `/actuator/metrics` endpoints |
| **Flyway** | 11.14.1 | SQL migration versioning |
| **PostgreSQL driver** | 42.7.5 | Production DB connector |
| **H2** | (test only) | In-memory DB for unit tests |
| **Lombok** | latest | `@Data`, `@Builder`, `@RequiredArgsConstructor` — removes boilerplate |
| **MapStruct** | 1.6.3 | Compile-time entity → DTO mapping |
| **jjwt** | 0.12.5 | JWT generation and validation |
| **Bucket4j** | 8.10.1 | IP-based rate limiting on the contact form |
| **SpringDoc OpenAPI** | 2.8.5 | Auto-generates Swagger UI at `/swagger-ui.html` |
| **Micrometer + Prometheus** | (starter) | Exports metrics to `/actuator/prometheus` |

### Why H2 is `testImplementation` only
Production uses PostgreSQL (NeonDB). H2 is only used during `./gradlew test` so tests run without needing a real database connection.

### Why MapStruct needs `annotationProcessor`
MapStruct generates Java implementation classes at **compile time**. Without the `annotationProcessor` in the config, the processor wouldn't run and no `ProfileMapperImpl.java` would be generated. It's not a runtime library.

---

## Package Structure Explained

```
app.vercel.dev_portfolio.portfolio/
│
├── PortfolioApplication.java   ← @SpringBootApplication - start here
│
├── config/           ← Infrastructure: security, JWT filter, CORS, health checks
├── controller/       ← HTTP layer: receive requests, validate, call service
├── service/          ← Business logic layer (interfaces + implementations)
│   └── impl/
├── repository/       ← Database layer: Spring Data JPA interfaces
├── entity/           ← JPA classes that map to DB tables
├── dto/              ← Data shapes sent to/received from the client
├── mapper/           ← MapStruct interfaces (entity ↔ DTO)
└── exception/        ← Global error handling
```

This is the classic **Layered Architecture** (also called N-Tier):

```
HTTP Request
    ↓
[Controller] — receives & validates
    ↓
[Service]    — business logic
    ↓
[Repository] — database queries
    ↓
[Entity]     — Java ↔ DB row mapping
    ↑
[Mapper]     — Entity → DTO (runs between service and controller)
    ↑
[DTO]        — what the client actually sees as JSON
```

Each layer only talks to the layer directly below it. A controller never calls a repository directly.

---

## Configuration — `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL}        # ← never hardcoded, read from .env
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Hibernate checks schema matches entities - doesn't create/alter tables
  flyway:
    enabled: true         # Flyway runs SQL migrations on startup

app:
  cloudinary:
    resume-url: ${CLOUDINARY_URL}   # Where the resume PDF lives
  jwt:
    secret: ${APP_JWT_SECRET}       # HMAC signing key
    expiration: ${APP_JWT_EXPIRATION} # Token TTL in ms

management:
  health:
    db.enabled: false       # Prevents SELECT 1 on every UptimeRobot ping (saves NeonDB compute)
    cloudinary.enabled: false
```

**Key insight**: `ddl-auto: validate` means Hibernate will **not** create or modify tables. Flyway owns the schema. Hibernate just validates that the entity classes match what's already in the DB.

---

## API Surface at a Glance

| Who calls it | Controller | Base path | Auth |
|---|---|---|---|
| Public (frontend) | ProfileController | `/api/v1/profile` | None |
| Public (frontend) | AboutController | `/api/v1/about` | None |
| Public (frontend) | WorkController | `/api/v1/work` | None |
| Public (visitors) | ContactController | `/api/v1/contact` | None |
| You (admin) | AuthController | `/api/v1/auth` | None (it's the login!) |
| You (admin) | AdminController | `/api/v1/admin` | JWT Bearer Token |
| Monitoring | Actuator | `/actuator/**` | ADMIN role |
| Dev tools | Swagger UI | `/swagger-ui.html` | None |
