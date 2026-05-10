# Step 10 — End-to-End Request Lifecycle Trace

> **Prompt**: "Trace a complete request lifecycle for two scenarios: (1) a public GET to /api/v1/about, and (2) a protected PUT to /api/v1/admin/profile with a JWT token. Show every class that gets involved."

---

## Scenario 1 — Public GET `/api/v1/about`

This is what happens when the portfolio frontend loads the About section. No JWT, no login required.

---

### Step-by-step trace:

**1. HTTP request arrives**
```
GET /api/v1/about
Host: portfolio-api.onrender.com
Origin: https://dev-portfolio-alpha-black.vercel.app
```

---

**2. `WebConfig.addCorsMappings` (CORS preflight, if browser)**

Before the actual GET, the browser may send an `OPTIONS` preflight:
```
OPTIONS /api/v1/about
Origin: https://dev-portfolio-alpha-black.vercel.app
```
`WebConfig` responds with:
```
Access-Control-Allow-Origin: https://dev-portfolio-alpha-black.vercel.app
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
```
Browser sees this → proceeds with the actual GET.

---

**3. `JwtAuthenticationFilter.doFilterInternal()`**

```java
final String authHeader = request.getHeader("Authorization");
// authHeader == null (no token sent)

if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);  // ← skip JWT processing, pass through
    return;
}
```

No token → `SecurityContextHolder` remains empty → filter passes the request to the next filter.

---

**4. `SecurityConfig` authorization check**

```java
.requestMatchers(HttpMethod.GET, "/api/v1/profile/**", "/api/v1/work", "/api/v1/about")
    .permitAll()
```

The URL `/api/v1/about` with method `GET` matches `.permitAll()` → no authentication required → request continues.

---

**5. Spring MVC `DispatcherServlet` matches the route**

Scans all `@RestController` beans, finds:
```java
@RestController
@RequestMapping("/api/v1/about")
public class AboutController {
    @GetMapping    // matches GET /api/v1/about
    public ResponseEntity<AboutFullResponse> getAboutData() { ... }
}
```
→ calls `AboutController.getAboutData()`

---

**6. `AboutController.getAboutData()`**

```java
public ResponseEntity<AboutFullResponse> getAboutData() {
    return ResponseEntity.ok(aboutService.getFullAboutData());
}
```
→ delegates immediately to `AboutServiceImpl`

---

**7. `AboutServiceImpl.getFullAboutData()` — the core logic**

```java
// Query 1: SELECT * FROM about_section WHERE id = 1
var left = aboutRepo.findById(1L)
    .orElseThrow(() -> new EntityNotFoundException("About section missing"));

// Query 2: SELECT sc.*, s.* FROM skill_categories sc LEFT JOIN skills s ON s.category_id = sc.id
List<SkillCategory> categories = skillRepo.findAllWithSkills();

// Query 3: SELECT * FROM awards
List<Awards> awards = awardRepo.findAll();

// Query 4: SELECT * FROM experience
List<Experience> experiences = expRepo.findAll();

// Query 5: SELECT * FROM certifications
List<Certifications> certs = certRepo.findAll();
```

Total: **5 SQL queries** fired.

---

**8. `AboutMapper` converts entities → DTOs**

```java
// mapper.toLeftDto(left):
// AboutSection { title, description, yearsExperience, projectsCompleted, techDebtReduced }
// → AboutLeftResponse { same fields }

// mapper.toSkillCategoryDto(category):
// SkillCategory { categoryName="Frontend & UI", skills=[Skill{skillName="React", iconName="FaReact"}] }
// → SkillCategoryDTO { title="Frontend & UI", icons=[SkillIconDTO{title="React", icon="FaReact"}] }

// mapper.awardToDto(award):
// Awards { title, stage, description }
// → TimelineDTO { title, stage, description }
```

---

**9. `AboutServiceImpl` assembles the final DTO**

```java
List<AboutTabDTO> tabs = List.of(
    new AboutTabDTO("skills",         skillCategories.stream().map(mapper::toSkillCategoryDto).toList()),
    new AboutTabDTO("awards",         awards.stream().map(mapper::awardToDto).toList()),
    new AboutTabDTO("experience",     experiences.stream().map(mapper::expToDto).toList()),
    new AboutTabDTO("certifications", certs.stream().map(mapper::certToDto).toList())
);
return new AboutFullResponse(mapper.toLeftDto(left), tabs);
```

---

**10. Jackson serializes to JSON**

Spring's `HttpMessageConverter` (Jackson) converts `AboutFullResponse` → JSON string:
```json
{
  "leftSection": {
    "title": "Robust architecture drives scalable systems.",
    "description": "Over the past 2.5+ years...",
    "yearsExperience": "2.5 +",
    "projectsCompleted": "10 +",
    "techDebtReduced": "40 %"
  },
  "tabs": [
    {
      "title": "skills",
      "info": [
        { "title": "Frontend & UI", "icons": [{"icon":"FaReact","title":"React"}, ...] }
      ]
    },
    { "title": "awards", "info": [{"title":"Microsoft AI Grant","stage":"2021-2022","description":"..."}] },
    ...
  ]
}
```

**11. HTTP 200 OK response sent to client**

---

## Scenario 2 — Protected PUT `/api/v1/admin/profile`

This is what happens when you (the admin) update your profile via the admin panel.

---

### Request:
```
PUT /api/v1/admin/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1...
Content-Type: application/json

{
  "name": "Ajay R Nair",
  "headline": "Transforming Ideas Into Scalable Architecture",
  "subHeadline": "Full Stack Developer...",
  "resumeUrl": "https://cloudinary.com/resume.pdf"
}
```

---

**1. CORS check** — same as Scenario 1, passes.

---

**2. `JwtAuthenticationFilter.doFilterInternal()`**

```java
final String authHeader = request.getHeader("Authorization");
// authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1..."

// Has "Bearer " prefix → proceed
final String jwt = authHeader.substring(7);
// jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1..."

// Extract username from token payload ("sub" claim)
final String userEmail = jwtService.extractUsername(jwt);
// → jwtService runs: Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload()
// → userEmail = "spamvinup@gmail.com"

// Load user from DB
UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
// → SELECT * FROM users WHERE email = 'spamvinup@gmail.com'
// → returns User entity with role "ROLE_ADMIN"

// Validate: username matches AND token not expired
if (jwtService.isTokenValid(jwt, userDetails)) {
    // Create authentication token
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()  // [ROLE_ADMIN]
    );
    // Store in SecurityContext for this request's thread
    SecurityContextHolder.getContext().setAuthentication(authToken);
}

filterChain.doFilter(request, response);  // continue to next filter
```

---

**3. `SecurityConfig` authorization check**

```java
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
```

SecurityContext has `ROLE_ADMIN` → ✓ → request proceeds.

---

**4. Spring MVC matches route → `AdminController.updateProfile()`**

```java
@PutMapping("/profile")
public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
    // Jackson deserializes the JSON body → Profile entity object
    // profile = Profile{ name="Ajay R Nair", headline="...", ... }

    profile.setId(1L);  // force singleton — always update row id=1

    // JPA save: finds existing row id=1 → runs UPDATE
    Profile saved = profileRepository.save(profile);
    // → UPDATE profile SET name=?, headline=?, sub_headline=?, ... WHERE id=1

    return ResponseEntity.ok(saved);  // returns the saved Profile entity as JSON
}
```

**Note**: `AdminController` returns the `Profile` entity directly (not a DTO). This is a design shortcut — fine for admin use, but exposes the full entity structure.

---

**5. HTTP 200 OK with the updated Profile entity as JSON**

```json
{
  "id": 1,
  "name": "Ajay R Nair",
  "headline": "Transforming Ideas Into Scalable Architecture",
  "subHeadline": "Full Stack Developer...",
  "resumeUrl": "https://cloudinary.com/resume.pdf",
  "email": null,
  "linkedinUrl": null,
  ...
}
```

---

## Bad Token Path — What Happens with Expired/Invalid JWT

```
PUT /api/v1/admin/profile
Authorization: Bearer eyJhbGci...EXPIRED_TOKEN
```

**2b. `JwtAuthenticationFilter`**:
```java
// jwtService.isTokenValid():
boolean isTokenExpired = extractClaim(token, Claims::getExpiration).before(new Date());
// isTokenExpired = true

// isTokenValid returns false
// → SecurityContextHolder NOT set
// → filter passes the request through anyway (filter doesn't block — SecurityConfig does)
```

**3b. `SecurityConfig` authorization check**:
```java
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
// SecurityContext is empty → no authentication found
// → Spring Security throws AccessDeniedException or returns 401
```

**Result**: HTTP 401 Unauthorized. `GlobalExceptionHandler` (or Spring Security's own entry point) returns an error response. **The controller never runs**.

---

## Class Involvement Summary

| Scenario | Classes Involved (in order) |
|---|---|
| GET /api/v1/about | `WebConfig` → `JwtAuthenticationFilter` → `SecurityConfig` → `AboutController` → `AboutServiceImpl` → 5 Repositories → `AboutMapper` → Jackson → HTTP 200 |
| PUT /api/v1/admin/profile (valid JWT) | `WebConfig` → `JwtAuthenticationFilter` → `JwtService` → `UserRepository` → `SecurityConfig` → `AdminController` → `ProfileRepository` → Jackson → HTTP 200 |
| PUT /api/v1/admin/profile (bad JWT) | `WebConfig` → `JwtAuthenticationFilter` → `JwtService` → `SecurityConfig` → HTTP 401 (controller never reached) |
| POST /api/v1/contact | `WebConfig` → `JwtAuthenticationFilter` (skip) → `SecurityConfig` → `ContactController` → `ContactServiceImpl` → Bucket4j rate limiter → `ContactRepository` → `@Async` Discord webhook → HTTP 200 |
