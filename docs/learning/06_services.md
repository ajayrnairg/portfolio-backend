# Step 6 — Service Layer (Interface + Implementation)

> **Prompt**: "Walk me through each service interface and its implementation. What business logic lives here, and how do they interact with repositories and mappers?"

---

## Why Interface + Implementation?

```java
// Controller depends on the interface:
private final AboutService aboutService;   // ← type is the interface

// Spring injects the implementation:
// Spring finds AboutServiceImpl (which @implements AboutService) and wires it in
```

Benefits:
- **Testable**: In unit tests, you can swap `aboutService` with a mock without changing the controller
- **Separation of concerns**: The interface declares *what* the service does; the impl decides *how*
- **DI principle**: "Depend on abstractions, not concrete classes"

---

## Service 1 — `AboutService` / `AboutServiceImpl`

**Interface**:
```java
public interface AboutService {
    AboutFullResponse getFullAboutData();
}
```

**Implementation**:
```java
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {
    private final AboutSectionRepository aboutRepo;
    private final SkillCategoryRepository skillRepo;
    private final AwardRepository awardRepo;
    private final ExperienceRepository expRepo;
    private final CertificationRepository certRepo;
    private final AboutMapper mapper;

    @Override
    public AboutFullResponse getFullAboutData() {
        // 1. Load the singleton about section (id=1)
        var left = aboutRepo.findById(1L)
            .orElseThrow(() -> new EntityNotFoundException("About section missing"));

        // 2. Build the 4 tabs by querying 4 separate tables
        List<AboutTabDTO> tabs = List.of(
            new AboutTabDTO("skills",
                skillRepo.findAllWithSkills().stream()    // custom @Query with JOIN FETCH
                    .map(mapper::toSkillCategoryDto).toList()),
            new AboutTabDTO("awards",
                awardRepo.findAll().stream()
                    .map(mapper::awardToDto).toList()),
            new AboutTabDTO("experience",
                expRepo.findAll().stream()
                    .map(mapper::expToDto).toList()),
            new AboutTabDTO("certifications",
                certRepo.findAll().stream()
                    .map(mapper::certToDto).toList())
        );

        // 3. Assemble and return the full response DTO
        return new AboutFullResponse(mapper.toLeftDto(left), tabs);
    }
}
```

**What it does**: Queries 5 separate tables (about_section, skill_categories+skills, awards, experience, certifications), maps all entities to DTOs, and assembles one flat response object. This is the most complex service.

---

## Service 2 — `ProfileService` / `ProfileServiceImpl`

**Interface**:
```java
public interface ProfileService {
    ProfileResponse getProfileData();
    String trackAndGetResumeUrl(HttpServletRequest request);
}
```

**Implementation** (key parts):
```java
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository repository;
    private final ProfileMapper mapper;
    private final DownloadLogRepository downloadLogRepository;
    private final MeterRegistry meterRegistry;   // Micrometer metrics
    private Counter resumeDownloadCounter;

    @Value("${app.cloudinary.resume-url}")
    private String resumeUrl;

    @PostConstruct
    public void init() {
        // Register a custom Prometheus counter on startup
        resumeDownloadCounter = Counter.builder("portfolio.resume.downloads")
                .description("Total number of resume downloads")
                .register(meterRegistry);
    }

    @Override
    public ProfileResponse getProfileData() {
        return repository.findById(1L)       // singleton — always id=1
                .map(mapper::toDto)          // map entity → DTO
                .orElseThrow(() -> new EntityNotFoundException("Profile record not found"));
    }

    @Transactional
    public String trackAndGetResumeUrl(HttpServletRequest request) {
        resumeDownloadCounter.increment();   // increments /actuator/metrics counter

        // Log the download with IP + User-Agent
        ResumeDownloadLog log = new ResumeDownloadLog();
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDownloadedAt(LocalDateTime.now());
        downloadLogRepository.save(log);

        return resumeUrl;  // the Cloudinary PDF URL read from application.yml
    }
}
```

**What's interesting**:
- `@PostConstruct` — runs after the bean is created. Used here to register a Prometheus counter that tracks total resume downloads. This metric appears at `/actuator/metrics/portfolio.resume.downloads`.
- `@Transactional` — wraps the download tracking in a DB transaction. If the save fails, it rolls back.
- `MeterRegistry` — Micrometer API for recording custom metrics.

---

## Service 3 — `WorkService` / `WorkServiceImpl`

```java
@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    private final ProjectRepository projectRepository;
    private final WorkSectionRepository sectionRepository;
    private final ProjectMapper mapper;

    public WorkSectionResponse getWorkData() {
        var section = sectionRepository.findById(1L).orElseThrow();  // singleton
        var projects = projectRepository.findAll();

        return new WorkSectionResponse(
                section.getTitle(),
                section.getDescription(),
                projects.stream().map(mapper::toDto).toList()
        );
    }
}
```

Simple: loads the singleton work section header, loads all projects, maps them to DTOs, assembles the response.

---

## Service 4 — `ContactService` / `ContactServiceImpl` (The Most Feature-Rich)

```java
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${DISCORD_WEBHOOK_URL}")
    private String discordUrl;

    // Rate limiter — 1 Bucket per IP, stored in memory
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @Override
    public void processMessage(ContactRequest request, String ip) {
        // 1. IP Rate Limiting via Bucket4j
        Bucket ipBucket = ipBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofDays(1))))
                .build()
        );

        if (!ipBucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Slow down! IP limit exceeded.");
        }

        // 2. Persist to DB
        repository.save(ContactMessage.builder()
                .name(request.name()).email(request.email())
                .subject(request.subject()).message(request.message())
                .ipAddress(ip).createdAt(LocalDateTime.now())
                .build());

        // 3. Fire Discord alert (async, doesn't block)
        sendDiscordNotification(request);
    }

    @Async   // ← runs in a background thread pool (enabled by @EnableAsync in main class)
    public void sendDiscordNotification(ContactRequest request) {
        Map<String, Object> payload = Map.of(
            "embeds", List.of(Map.of(
                "title", "📬 New Portfolio Message: " + request.subject(),
                "color", 5814783,
                "fields", List.of(
                    Map.of("name", "From",    "value", request.name(),    "inline", true),
                    Map.of("name", "Email",   "value", request.email(),   "inline", true),
                    Map.of("name", "Message", "value", request.message())
                )
            ))
        );
        try {
            restTemplate.postForEntity(discordUrl, payload, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send Discord alert: " + e.getMessage());
        }
    }
}
```

**Three things happening here**:

1. **Bucket4j rate limiting**: Each IP gets its own `Bucket` with 3 tokens, refilled daily. `tryConsume(1)` returns `false` if no tokens left → HTTP 429.
2. **Save to DB**: Uses `@Builder` pattern on `ContactMessage` entity.
3. **`@Async` Discord webhook**: `sendDiscordNotification` is annotated `@Async`. Spring runs it in a separate thread pool (configured by `@EnableAsync`). The HTTP response returns to the user **immediately** while Discord gets notified in the background. If Discord fails, the error is logged but doesn't affect the user.

---

## Service 5 — `JwtService` (No Interface — It's a Utility)

```java
@Service
public class JwtService {
    @Value("${app.jwt.secret}")     private String secretKey;
    @Value("${app.jwt.expiration}") private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);  // reads "sub" from JWT payload
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())           // "sub": email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())                     // HMAC-SHA256
                .compact();                                   // → "eyJ..."
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // Parses + verifies the JWT signature, returns payload claims
        final Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);    // turns base64 string → HMAC key
    }
}
```

**The flow**: `APP_JWT_SECRET` in `.env` is a base64-encoded string. `getSignInKey()` decodes it into bytes and creates an HMAC-SHA256 key. This key is used to sign tokens on generation and verify signatures on validation.
