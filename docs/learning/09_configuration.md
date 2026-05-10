# Step 9 — Configuration & Cross-Cutting Concerns

> **Prompt**: "Explain WebConfig (CORS), GlobalExceptionHandler, CloudinaryHealthIndicator, and the application.yml. What cross-cutting concerns are handled and why?"

---

## What "Cross-Cutting Concerns" Means

A cross-cutting concern is something that affects **every request**, regardless of which controller or service handles it. Examples:
- Error formatting (every failed request needs a consistent error response)
- CORS (every request from a browser needs CORS headers)
- Health monitoring (every deployment needs a health check)
- Observability (every request can be measured)

---

## `WebConfig.java` — CORS

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(
                    "https://dev-portfolio-alpha-black.vercel.app",  // production frontend
                    "http://localhost:3000"                           // local dev frontend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

**Why CORS is needed**: Browsers enforce the Same-Origin Policy. When your Next.js frontend (on `vercel.app`) calls this backend (on `onrender.com`), the browser sends a **preflight** `OPTIONS` request first, asking "does this server allow cross-origin requests from my origin?". Without CORS config, the backend doesn't respond correctly to that preflight, and the browser blocks the actual request.

**`allowCredentials(true)`**: Allows the browser to send cookies and the `Authorization` header cross-origin.

**`allowedHeaders("*")`**: Accepts any request header, including `Authorization: Bearer ...`.

**How `SecurityConfig` wires it**: `.cors(Customizer.withDefaults())` in `SecurityConfig` tells Spring Security to use the CORS config from the `WebConfig` bean.

---

## `GlobalExceptionHandler.java` — Centralized Error Handling

```java
@RestControllerAdvice   // intercepts exceptions from ANY @RestController
public class GlobalExceptionHandler {

    // 404 — entity not found (thrown in services with .orElseThrow())
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),   // 404
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 403 — user is authenticated but doesn't have the right role
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "error", "Access Denied",
                "message", "You do not have permission to perform this action."
        ));
    }

    // 401 — wrong email or password at login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "error", "Unauthorized",
                "message", "Invalid email or password."
        ));
    }

    // 500 — everything else (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
                "An unexpected error occurred",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**`@RestControllerAdvice`** = `@ControllerAdvice` + `@ResponseBody`. It catches exceptions thrown by any `@RestController` and lets you return a clean JSON error response instead of Spring's default HTML error page.

**`ErrorResponse` DTO**:
```java
public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
```

**What's NOT handled explicitly**:
- `MethodArgumentNotValidException` (from `@Valid` failure) — Spring Boot's default handler returns a 400 with validation detail. The `Exception.class` catch-all would override it with a 500 if it ran first, but Spring's specific handler runs with higher priority.
- `ResponseStatusException` from `ContactServiceImpl` (rate limiting 429) — Spring handles `ResponseStatusException` automatically using the status from the exception.

**Exception priority**: More specific handlers (`EntityNotFoundException`) take priority over the catch-all (`Exception`). Spring selects the most specific matching handler.

---

## `CloudinaryHealthIndicator.java` — Custom Actuator Health Check

```java
@Component
public class CloudinaryHealthIndicator implements HealthIndicator {

    @Value("${app.cloudinary.resume-url}")
    private String resumeUrl;

    @Override
    public Health health() {
        try {
            // Sends an HTTP HEAD request to the Cloudinary URL
            var connection = (HttpURLConnection) new URL(resumeUrl).openConnection();
            connection.setRequestMethod("HEAD");  // HEAD = get headers only, no body
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return Health.up().withDetail("status", responseCode).build();
            }
            return Health.down().withDetail("status", responseCode).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

This would make `/actuator/health` show:
```json
{
  "components": {
    "cloudinary": { "status": "UP", "details": { "status": 200 } }
  }
}
```

**BUT it's disabled** in `application.yml`:
```yaml
management:
  health:
    cloudinary:
      enabled: false
```

**Why disabled**: UptimeRobot pings `/actuator/health` every few minutes to check if the app is alive. If `CloudinaryHealthIndicator` were enabled, every ping would make an HTTP call to Cloudinary. That's unnecessary traffic. Disabled = the component doesn't appear in the health response, but the app stays running.

---

## `application.yml` — Complete Walkthrough

```yaml
spring:
  datasource:
    url: ${DB_URL}                        # e.g. jdbc:postgresql://neon.tech/neondb?sslmode=require
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect   # tells Hibernate it's Postgres
    hibernate:
      ddl-auto: validate                  # Hibernate validates schema; Flyway creates it
                                          # (update/create would overwrite Flyway's work)

  flyway:
    enabled: true
    baseline-on-migrate: true             # allows Flyway to work on a pre-existing DB

app:
  cloudinary:
    resume-url: ${CLOUDINARY_URL}         # the Cloudinary PDF link
  jwt:
    secret: ${APP_JWT_SECRET}             # base64-encoded HMAC key
    expiration: ${APP_JWT_EXPIRATION}     # token TTL in milliseconds (e.g. 86400000 = 1 day)

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus   # which /actuator/* endpoints are accessible
  endpoint:
    health:
      show-details: when-authorized       # detailed health info only for authenticated users
      probes:
        enabled: true                     # enables /actuator/health/liveness and /readiness
                                          # (important for Render's zero-downtime deploy)
  health:
    db:
      enabled: false                      # prevents SELECT 1 on every UptimeRobot ping
    cloudinary:
      enabled: false                      # prevents HTTP call to Cloudinary on every ping
  metrics:
    tags:
      application: portfolio-backend      # adds this tag to all Prometheus metrics
  info:
    env:
      enabled: true                       # allows /actuator/info to show env info

logging:
  level:
    org.springframework.security.config...: ERROR   # silences a noisy Spring Security startup log
```

---

## Environment Variables Used

Stored in `.env` (local) or Render's environment variable settings (production):

| Variable | Example Value | Used In |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://ep-abc.neon.tech/neondb?sslmode=require` | `application.yml` datasource |
| `DB_USERNAME` | `neondb_owner` | `application.yml` datasource |
| `DB_PASSWORD` | `abc123` | `application.yml` datasource |
| `CLOUDINARY_URL` | `https://res.cloudinary.com/.../resume.pdf` | `application.yml` + `ProfileServiceImpl` |
| `APP_JWT_SECRET` | `base64-string` | `JwtService` |
| `APP_JWT_EXPIRATION` | `86400000` | `JwtService` |
| `DISCORD_WEBHOOK_URL` | `https://discord.com/api/webhooks/...` | `ContactServiceImpl` |

**How `.env` is loaded for local dev** (from `build.gradle`):
```groovy
tasks.named('bootRun') {
    def envFile = file('.env')
    if (envFile.exists()) {
        envFile.readLines().each { line ->
            if (line.trim() && !line.startsWith('#')) {
                def parts = line.split('=', 2)
                if (parts.length == 2) {
                    environment parts[0].trim(), parts[1].trim()
                }
            }
        }
    }
}
```
When you run `./gradlew bootRun`, Gradle reads `.env` and injects each key-value pair as an environment variable. In production on Render, these are set directly in the dashboard.
