# Step 4 — Repositories

> **Prompt**: "Explain the repository interfaces. What Spring Data JPA magic do they use, and are there any custom queries defined?"

---

## How Spring Data JPA Repositories Work

Every repository in this project is an **interface** — not a class. You never write an implementation. At startup, Spring Data JPA scans for interfaces extending `JpaRepository` and generates a **runtime proxy class** that implements all methods automatically.

```java
// All you write:
public interface ProfileRepository extends JpaRepository<Profile, Long> { }

// Spring auto-generates at runtime (you never see this):
class ProfileRepositoryImpl implements ProfileRepository {
    public Optional<Profile> findById(Long id) {
        return entityManager.find(Profile.class, id);
    }
    public Profile save(Profile entity) { ... }
    public void deleteById(Long id) { ... }
    // ...and ~20 more methods
}
```

---

## Free Methods From `JpaRepository<T, ID>`

Every repository gets these for free without writing a single line:

| Method | SQL it runs |
|---|---|
| `findAll()` | `SELECT * FROM table` |
| `findById(id)` | `SELECT * FROM table WHERE id = ?` |
| `save(entity)` | `INSERT` if new, `UPDATE` if existing |
| `deleteById(id)` | `DELETE FROM table WHERE id = ?` |
| `deleteAllById(ids)` | `DELETE FROM table WHERE id IN (...)` |
| `count()` | `SELECT COUNT(*) FROM table` |
| `existsById(id)` | `SELECT COUNT(*) > 0 WHERE id = ?` |
| `findAll(Sort)` | `SELECT * FROM table ORDER BY ...` |

---

## The Repositories in This Project

### Standard repositories (no custom methods):

All of these just extend `JpaRepository` with no extra methods. The free methods are enough:

```java
public interface ProfileRepository extends JpaRepository<Profile, Long> { }
public interface AboutSectionRepository extends JpaRepository<AboutSection, Long> { }
public interface ExperienceRepository extends JpaRepository<Experience, Long> { }
public interface AwardRepository extends JpaRepository<Awards, Long> { }
public interface CertificationRepository extends JpaRepository<Certifications, Long> { }
public interface ProjectRepository extends JpaRepository<Project, Long> { }
public interface WorkSectionRepository extends JpaRepository<WorkSection, Long> { }
public interface ContactRepository extends JpaRepository<ContactMessage, Long> { }
public interface DownloadLogRepository extends JpaRepository<ResumeDownloadLog, Long> { }
public interface SkillRepository extends JpaRepository<Skill, Long> { }
```

---

### `UserRepository` — method name query derivation

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

Spring Data reads `findByEmail` and generates:
```sql
SELECT * FROM users WHERE email = ?
```

- Returns `Optional<User>` — safe for when no user is found
- Used in `ApplicationConfig` for login: `userRepository.findByEmail(username)`
- This is the **only** way the auth system loads the admin user

---

### `SkillCategoryRepository` — the only `@Query` in the project

```java
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
    @Query("SELECT sc FROM SkillCategory sc LEFT JOIN FETCH sc.skills")
    List<SkillCategory> findAllWithSkills();
}
```

This is **JPQL** (Java Persistence Query Language — like SQL but against entity names).

**Why is this needed?** Because by default, `@OneToMany` collections use **lazy loading** — Hibernate only fetches the `skills` list when you actually access it. In `AboutServiceImpl`, the code streams over skill categories and maps them to DTOs. Without `JOIN FETCH`, this would cause an **N+1 query problem**: 1 query to get all categories, then 1 query per category to load its skills. `LEFT JOIN FETCH` loads everything in **one query**.

The generated SQL looks like:
```sql
SELECT sc.*, s.*
FROM skill_categories sc
LEFT JOIN skills s ON s.category_id = sc.id
```

---

## Query Derivation Rules (How Method Names Become SQL)

Spring Data parses the method name left-to-right:

| Method name part | Meaning |
|---|---|
| `findBy` | `SELECT * FROM ... WHERE` |
| `findAllBy` | same as above |
| `deleteBy` | `DELETE FROM ... WHERE` |
| `countBy` | `SELECT COUNT(*) FROM ... WHERE` |
| `Email` | `email = ?` |
| `EmailAndName` | `email = ? AND name = ?` |
| `NameContaining` | `name LIKE %?%` |
| `CreatedAtAfter` | `created_at > ?` |
| `OrderByCreatedAtDesc` | `ORDER BY created_at DESC` |

Example you could add:
```java
List<ContactMessage> findByEmailOrderByCreatedAtDesc(String email);
// → SELECT * FROM contact_messages WHERE email = ? ORDER BY created_at DESC
```

---

## How `AdminController` Uses Repositories Directly

Notice that `AdminController` **injects repositories directly** — it skips the service layer:

```java
@RestController
public class AdminController {
    private final ProfileRepository profileRepository;
    private final ExperienceRepository experienceRepository;
    // ...

    @PutMapping("/profile")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
        profile.setId(1L);                         // force singleton ID
        return ResponseEntity.ok(profileRepository.save(profile));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceRepository.deleteById(id);
        return ResponseEntity.noContent().build();  // HTTP 204
    }
}
```

This is a **design shortcut** — for simple CRUD (no business logic needed), going directly to the repository from the controller is acceptable. Purists would prefer a service layer here too.

---

## `ContactRepository` — used with `Sort`

```java
// In AdminController:
contactRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
// → SELECT * FROM contact_messages ORDER BY created_at DESC
```

`findAll(Sort)` is a free method from `JpaRepository`. `Sort.by(...)` is a Spring Data object that generates the `ORDER BY` clause.
