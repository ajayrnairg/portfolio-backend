# Step 8 — Security, JWT & Authentication

> **Prompt**: "Explain the full security setup: SecurityConfig, JwtAuthenticationFilter, JwtService, and ApplicationConfig. How does JWT authentication work end-to-end from login to accessing a protected endpoint?"

---

## The 4 Files That Own Security

| File | Role |
|---|---|
| `ApplicationConfig.java` | Creates the core Spring Security beans: `UserDetailsService`, `PasswordEncoder`, `AuthenticationProvider`, `AuthenticationManager` |
| `SecurityConfig.java` | Defines the filter chain: which routes are public, which need JWT, session policy |
| `JwtAuthenticationFilter.java` | Runs on every request: extracts + validates the JWT, sets the security context |
| `JwtService.java` | Token utility: generates, parses, and validates JWT strings |

---

## `ApplicationConfig.java` — The Bean Factory

```java
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // 1. How to load a user by username (email in this app)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // 2. Wires UserDetailsService + PasswordEncoder together
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. The manager that runs the actual credential check
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. BCrypt — one-way hash for passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**How these connect**:
- `userDetailsService` → fetches the admin user from the DB by email
- `authenticationProvider` (DaoAuthenticationProvider) → calls `userDetailsService.loadUserByUsername()`, then uses `passwordEncoder.matches(rawPassword, storedHash)` to verify
- `authenticationManager` → orchestrates the whole authentication process; called in `AuthController.login()`
- `BCryptPasswordEncoder` → used to hash passwords at registration and verify them at login

**`User` entity implements `UserDetails`**: Spring Security's `UserDetailsService` must return a `UserDetails` object. The `User` entity directly implements this interface, so it IS the UserDetails. `getUsername()` returns email, `getPassword()` returns the BCrypt hash, `getAuthorities()` returns `[ROLE_ADMIN]`.

---

## `SecurityConfig.java` — The Filter Chain Rules

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF — stateless JWT apps don't need it
            //    (CSRF protection is for cookie-based sessions, which this app doesn't use)
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Enable CORS using WebConfig bean (allows frontend origins)
            .cors(Customizer.withDefaults())

            // 3. Route-level authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()          // UptimeRobot health pings
                .requestMatchers("/actuator/**").hasRole("ADMIN")            // Other actuator needs auth
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/profile/**",
                    "/api/v1/work",
                    "/api/v1/about").permitAll()                             // Public read endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/contact").permitAll() // Contact form
                .requestMatchers("/api/v1/auth/**").permitAll()              // Login endpoint
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger UI
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")       // All admin routes
                .anyRequest().authenticated()                                // Everything else locked
            )

            // 4. No HTTP sessions — each request must carry its own JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 5. Use our custom auth provider (DB-backed)
            .authenticationProvider(authenticationProvider)

            // 6. Run JWT filter BEFORE Spring's default UsernamePassword filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**Why `hasRole("ADMIN")` and not `hasAuthority("ROLE_ADMIN")`?**
Spring Security's `hasRole()` automatically prepends `ROLE_`. `hasRole("ADMIN")` is equivalent to `hasAuthority("ROLE_ADMIN")`. The `User` entity stores `"ROLE_ADMIN"` in the `role` field.

---

## `JwtAuthenticationFilter.java` — Runs on Every Request

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Check Authorization header
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token → just pass through (public routes will still work)
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the raw JWT (strip "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        // 3. Extract the email from the token payload
        final String userEmail = jwtService.extractUsername(jwt);

        // 4. If we got an email AND the user isn't already authenticated in this request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Load the user from DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate: does token username match DB user? Is token expired?
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Create an authentication object with the user's roles
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                         // credentials (null = already validated)
                        userDetails.getAuthorities()  // [ROLE_ADMIN]
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Store in SecurityContext — now this request is "authenticated"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Always call the next filter, regardless of auth result
        filterChain.doFilter(request, response);
    }
}
```

**`OncePerRequestFilter`** — guarantees `doFilterInternal` runs exactly once per HTTP request (even if the request is forwarded internally).

**`SecurityContextHolder`** — thread-local storage for the current request's authentication. After step 8, any code in the same request (controller, service) can call `SecurityContextHolder.getContext().getAuthentication()` to get the current user.

---

## `JwtService.java` — Token Mechanics

```java
// GENERATE a token on login:
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .subject(userDetails.getUsername())          // "sub": "admin@email.com"
            .issuedAt(new Date(System.currentTimeMillis()))  // "iat": now
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // "exp": now + TTL
            .signWith(getSignInKey())                    // HMAC-SHA256 signature
            .compact();                                  // → "eyJhbGci..."
}

// VALIDATE a token on protected request:
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}

// The signing key: APP_JWT_SECRET (base64 string) → decoded → HMAC key
private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

**JWT Structure** (`eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1...`):
```
HEADER.PAYLOAD.SIGNATURE

Header: { "alg": "HS256" }
Payload: { "sub": "admin@email.com", "iat": 1234567890, "exp": 1234654290 }
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
```

The signature means: **only the server that knows `APP_JWT_SECRET` can create or verify tokens**. Anyone can decode the header+payload (it's just base64), but can't forge a valid signature without the secret.

---

## Full Authentication Flow — Login

```
1. POST /api/v1/auth/login  { "email": "admin@...", "password": "admin123" }

2. AuthController.login()
   → authenticationManager.authenticate(
         new UsernamePasswordAuthenticationToken("admin@...", "admin123")
     )

3. DaoAuthenticationProvider (configured in ApplicationConfig)
   → calls userDetailsService.loadUserByUsername("admin@...")
   → runs: SELECT * FROM users WHERE email = 'admin@...'
   → returns User entity (which implements UserDetails)
   → calls passwordEncoder.matches("admin123", "$2a$10$...hash...")
   → BCrypt verification: true

4. Authentication succeeds
   → AuthController continues

5. userDetailsService.loadUserByUsername("admin@...") again (reloads)
6. jwtService.generateToken(user) → "eyJhbGci..."

7. ResponseEntity.ok(new AuthResponse("eyJhbGci..."))
   HTTP 200: { "token": "eyJhbGci..." }
```

---

## Full Authentication Flow — Protected Request

```
1. PUT /api/v1/admin/profile
   Authorization: Bearer eyJhbGci...

2. JwtAuthenticationFilter runs:
   → extracts "eyJhbGci..." from header
   → jwtService.extractUsername(token) → "admin@..."
   → userDetailsService.loadUserByUsername("admin@...") → User entity
   → jwtService.isTokenValid(token, user):
       - username matches? ✓
       - not expired? ✓
   → sets SecurityContextHolder with [ROLE_ADMIN] authentication

3. SecurityConfig checks:
   → /api/v1/admin/** requires hasRole("ADMIN")
   → SecurityContext has ROLE_ADMIN ✓
   → request proceeds to AdminController

4. AdminController.updateProfile() executes
   → returns 200 OK

--- BAD TOKEN PATH ---
2b. JwtAuthenticationFilter:
   → jwtService.isTokenValid() → false (expired or wrong signature)
   → SecurityContextHolder NOT set

3b. SecurityConfig checks:
   → /api/v1/admin/** requires ADMIN role
   → SecurityContext is empty
   → HTTP 401 Unauthorized returned immediately
   → Controller never runs
```
