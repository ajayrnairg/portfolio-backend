# Step 7 — Controllers & API Design

> **Prompt**: "Explain each controller class. What endpoints does each expose, what annotations are used, how is validation done, and how does request flow through to the service layer?"

---

## Controller Design Rule

Controllers in this project are deliberately **thin** — they only:
1. Receive the HTTP request
2. Extract parameters / deserialize the body
3. Optionally call `@Valid` validation
4. Call one service method
5. Return a `ResponseEntity`

No SQL, no business logic, no data transformation happens in a controller.

---

## Core Annotations

| Annotation | Meaning |
|---|---|
| `@RestController` | `@Controller` + `@ResponseBody` — all methods return JSON automatically |
| `@RequestMapping("/api/v1/x")` | Base URL prefix for all methods in the class |
| `@GetMapping("/path")` | Handles `GET /api/v1/x/path` |
| `@PostMapping("/path")` | Handles `POST /api/v1/x/path` |
| `@PutMapping("/path")` | Handles `PUT /api/v1/x/path` |
| `@PatchMapping("/path")` | Handles `PATCH /api/v1/x/path` |
| `@DeleteMapping("/path/{id}")` | Handles `DELETE /api/v1/x/path/{id}` |
| `@PathVariable Long id` | Extracts `{id}` from the URL path |
| `@RequestBody` | Deserializes JSON body into a DTO/entity |
| `@Valid` | Triggers Bean Validation on the `@RequestBody` |
| `ResponseEntity<T>` | Wraps response with HTTP status code + body |
| `@RequiredArgsConstructor` | Lombok: generates constructor for all `final` fields (DI) |

---

## Controller 1 — `AuthController`

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Step 1: AuthenticationManager verifies email + password against DB
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        // Step 2: Load the UserDetails for the validated user
        var user = userDetailsService.loadUserByUsername(request.email());
        // Step 3: Generate a JWT token
        var token = jwtService.generateToken(user);
        // Step 4: Return just the token
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

**What happens if credentials are wrong?** `authenticationManager.authenticate()` throws `BadCredentialsException`, which is caught by `GlobalExceptionHandler` and returns HTTP 401.

---

## Controller 2 — `ProfileController`

```java
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for Home/Hero section")  // Swagger grouping
public class ProfileController {

    private final ProfileService profileService;
    private final DownloadLogRepository downloadLogRepository;  // used directly for stats

    // GET /api/v1/profile/getProfile
    @GetMapping("/getProfile")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfileData());
    }

    // GET /api/v1/profile/resume/download
    @GetMapping("/resume/download")
    public ResponseEntity<Void> downloadResume(HttpServletRequest request) {
        String cloudinaryUrl = profileService.trackAndGetResumeUrl(request);
        // HTTP 302 Redirect — browser follows the Location header to the Cloudinary PDF
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(cloudinaryUrl))
                .build();
    }

    // GET /api/v1/profile/resume/stats
    @GetMapping("/resume/stats")
    public ResponseEntity<Map<String, Object>> getDownloadStats() {
        List<ResumeDownloadLog> downloadsInfo = downloadLogRepository.findAll();
        return ResponseEntity.ok(Map.of("downloads", downloadsInfo));
    }
}
```

**The redirect pattern**: `ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build()` returns HTTP 302. The browser automatically follows it to the Cloudinary URL. The user never sees the Cloudinary URL in their original request — they just get the PDF.

**Note**: `ProfileController` injects `DownloadLogRepository` **directly** for the stats endpoint — bypassing the service layer. This is another design shortcut for a simple read.

---

## Controller 3 — `AboutController`

```java
@RestController
@RequestMapping("/api/v1/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping   // handles GET /api/v1/about
    public ResponseEntity<AboutFullResponse> getAboutData() {
        return ResponseEntity.ok(aboutService.getFullAboutData());
    }
}
```

The simplest controller — one method, one endpoint, delegates everything to the service.

---

## Controller 4 — `WorkController`

```java
@RestController
@RequestMapping("/api/v1/work")
@RequiredArgsConstructor
@Tag(name = "Work", description = "Endpoints for Featured Engineering/Projects section")
public class WorkController {

    private final WorkService workService;

    @GetMapping   // handles GET /api/v1/work
    public ResponseEntity<WorkSectionResponse> getWorkData() {
        return ResponseEntity.ok(workService.getWorkData());
    }
}
```

Same pattern — one endpoint, delegates to service.

---

## Controller 5 — `ContactController`

```java
@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService service;

    @PostMapping   // handles POST /api/v1/contact
    public ResponseEntity<Map<String, String>> sendMessage(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        service.processMessage(request, httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of("message", "Talk to you soon, Ajay!"));
    }
}
```

**Key patterns**:
- `@Valid @RequestBody` — Jackson deserializes the JSON body into `ContactRequest`, then Bean Validation runs. If `@NotBlank`, `@Email`, or `@Size` fail → `MethodArgumentNotValidException` → `GlobalExceptionHandler` returns HTTP 400.
- `httpRequest.getRemoteAddr()` — extracts the caller's IP for rate limiting in the service
- The response is a hardcoded `Map.of("message", "Talk to you soon, Ajay!")` — simple, not a DTO

---

## Controller 6 — `AdminController` (The Largest)

All 17 endpoints under `/api/v1/admin/**` are here. They all require JWT (enforced by `SecurityConfig`, not by the controller itself):

```java
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Management endpoints for all sections")
@SecurityRequirement(name = "bearerAuth")   // Swagger UI shows the lock icon
public class AdminController {
    // Injects 9 repositories directly (no service layer for admin CRUD)
    private final ProfileRepository profileRepository;
    private final AboutSectionRepository aboutSectionRepository;
    private final ExperienceRepository experienceRepository;
    private final AwardRepository awardRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillRepository skillRepository;
    private final ContactRepository contactRepository;
```

### Pattern: Singleton Update (Profile, AboutSection)
```java
@PutMapping("/profile")
public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
    profile.setId(1L);   // force ID=1 — you can't create a second profile
    return ResponseEntity.ok(profileRepository.save(profile));
}
```
`save()` does an `UPDATE` if the entity exists, `INSERT` if new. Since ID=1 is forced, it always updates.

### Pattern: Simple CRUD (Experience, Awards, Certifications, Projects)
```java
@PostMapping("/experience")
public ResponseEntity<Experience> addExperience(@RequestBody Experience exp) {
    return ResponseEntity.ok(experienceRepository.save(exp));   // INSERT
}

@PutMapping("/experience/{id}")
public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience exp) {
    exp.setId(id);   // force the ID from the URL — prevents accidental mismatch
    return ResponseEntity.ok(experienceRepository.save(exp));   // UPDATE
}

@DeleteMapping("/experience/{id}")
public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
    experienceRepository.deleteById(id);
    return ResponseEntity.noContent().build();   // HTTP 204 — success, no body
}
```

### Pattern: Nested Resource (Skills under SkillCategories)
```java
@PostMapping("/skill-categories/{categoryId}/skills")
public ResponseEntity<Skill> addSkill(@PathVariable Long categoryId, @RequestBody Skill skill) {
    SkillCategory category = skillCategoryRepository.findById(categoryId)
             .orElseThrow(() -> new RuntimeException("Category not found"));
    skill.setCategory(category);   // set the FK relationship manually
    return ResponseEntity.ok(skillRepository.save(skill));
}
```

### Pattern: PATCH for partial updates
```java
@PatchMapping("/profile/contact-info")
public ResponseEntity<Profile> updateContactInfo(@RequestBody Map<String, String> contactUpdates) {
    Profile p = profileRepository.findById(1L).orElseThrow(...);
    if (contactUpdates.containsKey("email"))       p.setEmail(contactUpdates.get("email"));
    if (contactUpdates.containsKey("githubUrl"))   p.setGithubUrl(contactUpdates.get("githubUrl"));
    // ... other social links
    return ResponseEntity.ok(profileRepository.save(p));
}
```
`PATCH` = partial update. Client only sends the fields they want to change. A `Map<String, String>` body is used instead of a DTO — flexible but less type-safe.

---

## Response Codes Summary

| Controller method | Returns |
|---|---|
| Successful GET | `200 OK` + JSON body |
| Successful POST | `200 OK` + created entity JSON |
| Successful PUT/PATCH | `200 OK` + updated entity JSON |
| Successful DELETE | `204 No Content` (empty body) |
| Resume download | `302 Found` + Location header |
| `@Valid` failure | `400 Bad Request` (from `GlobalExceptionHandler`) |
| Bad credentials | `401 Unauthorized` (from `GlobalExceptionHandler`) |
| No JWT on admin route | `401 Unauthorized` (from Spring Security) |
| Wrong role | `403 Forbidden` (from `GlobalExceptionHandler`) |
| Entity not found | `404 Not Found` (from `GlobalExceptionHandler`) |
