# Step 5 — DTOs & MapStruct Mappers

> **Prompt**: "Explain the DTO classes and MapStruct mappers. Why are DTOs used instead of returning entities directly, and how does the mapping flow from entity → DTO → response?"

---

## Why Not Return Entities Directly?

Three concrete problems with returning a JPA entity as JSON:

1. **Exposes DB internals** — `User.password` (the BCrypt hash) would appear in JSON
2. **Circular references** — `SkillCategory` has a `List<Skill>`, and `Skill` has a `SkillCategory` reference → infinite loop when Jackson tries to serialize
3. **Coupling** — renaming a DB column forces a frontend change; DTOs decouple them

---

## What MapStruct Does

MapStruct is a **compile-time code generator**. You write an interface:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {
    ProfileResponse toDto(Profile profile);
}
```

At compile time, MapStruct generates a real class in `build/generated/sources/annotationProcessor/`:

```java
// Auto-generated — you never write this:
@Component
public class ProfileMapperImpl implements ProfileMapper {
    @Override
    public ProfileResponse toDto(Profile profile) {
        if (profile == null) return null;
        return new ProfileResponse(
            profile.getName(),
            profile.getHeadline(),
            profile.getSubHeadline(),
            profile.getResumeUrl()
            // only the fields ProfileResponse has — nothing extra
        );
    }
}
```

Spring injects `ProfileMapperImpl` wherever `ProfileMapper` is declared as a dependency. Zero reflection, zero runtime overhead.

---

## The 3 Mappers in This Project

### `ProfileMapper.java`

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {
    ProfileResponse toDto(Profile profile);
}
```

Dead simple — field names in `Profile` match field names in `ProfileResponse`, so no `@Mapping` annotations needed. MapStruct matches by name automatically.

---

### `ProjectMapper.java`

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    @Mapping(target = "path", source = "thumbnailPath")   // name mismatch
    ProjectDTO toDto(Project project);

    @Mapping(target = "title", source = "label")          // name mismatch
    @Mapping(target = "icon", source = "iconName")        // name mismatch
    LinkDTO toLinkDto(ProjectLink link);
}
```

`@Mapping(target = "path", source = "thumbnailPath")` — entity has `thumbnailPath`, but the DTO calls it `path`. MapStruct uses this annotation to wire the mismatch.

MapStruct also auto-maps `List<ProjectLink> ctaLinks` → `List<LinkDTO>` by repeatedly calling `toLinkDto()` on each element.

---

### `AboutMapper.java`

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AboutMapper {
    AboutLeftResponse toLeftDto(AboutSection entity);

    TimelineDTO awardToDto(Awards entity);
    TimelineDTO expToDto(Experience entity);
    TimelineDTO certToDto(Certifications entity);

    @Mapping(target = "title", source = "categoryName")
    @Mapping(target = "icons", source = "skills")
    SkillCategoryDTO toSkillCategoryDto(SkillCategory entity);

    @Mapping(target = "icon", source = "iconName")
    @Mapping(target = "title", source = "skillName")
    SkillIconDTO toSkillIconDto(Skill entity);
}
```

Note that `Awards`, `Experience`, and `Certifications` all map to the **same** `TimelineDTO`. This is valid because all three entities have the same fields (`title`, `stage`, `description`).

The `toSkillCategoryDto` method maps `skills` (a `List<Skill>`) → `icons` (a `List<SkillIconDTO>`). MapStruct sees `toSkillIconDto` in the same mapper and automatically uses it to convert each `Skill`.

---

## The DTOs in This Project

### Request DTOs (Client → Server)

```java
// LoginRequest.java — used in AuthController
public record LoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {}

// ContactRequest.java — used in ContactController
public record ContactRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    String subject,             // optional
    @Size(min = 10) @NotBlank String message
) {}
```

These use Java **records** — immutable, no setters needed. The validation annotations (`@NotBlank`, `@Email`, `@Size`) are activated by `@Valid` in the controller.

---

### Response DTOs (Server → Client)

```java
// ProfileResponse — what GET /api/v1/profile/getProfile returns
public record ProfileResponse(String name, String headline, String subHeadline, String resumeUrl) {}

// WorkSectionResponse — what GET /api/v1/work returns
public record WorkSectionResponse(String title, String description, List<ProjectDTO> projects) {}

// ProjectDTO — nested inside WorkSectionResponse
public record ProjectDTO(String title, String path, String description,
                         List<String> techStack, List<LinkDTO> ctaLinks) {}

// LinkDTO — nested inside ProjectDTO
public record LinkDTO(String title, String icon, String url) {}
```

---

### The `AboutFullResponse` — the most complex shape

```java
// What GET /api/v1/about returns:
public record AboutFullResponse(AboutLeftResponse leftSection, List<AboutTabDTO> tabs) {}

// The left panel:
public record AboutLeftResponse(String title, String description,
                                String yearsExperience, String projectsCompleted, String techDebtReduced) {}

// Each tab in the right panel:
public record AboutTabDTO(String title, List<?> info) {}
// title is "skills", "awards", "experience", or "certifications"
// info is a List<SkillCategoryDTO> or List<TimelineDTO> depending on the tab
```

The `AboutServiceImpl` manually constructs the tab list with 4 items.

---

## Mapping Flow — End to End

```
DB Row (PostgreSQL)
    ↓ Hibernate
Entity object (e.g. SkillCategory with List<Skill>)
    ↓ AboutMapper.toSkillCategoryDto()
SkillCategoryDTO(title = "Frontend & UI", icons = [SkillIconDTO("FaReact", "React"), ...])
    ↓ wrapped in AboutTabDTO("skills", [...])
    ↓ wrapped in AboutFullResponse
    ↓ Jackson serializes to JSON
HTTP Response body:
{
  "leftSection": { "title": "...", "yearsExperience": "2.5 +" },
  "tabs": [
    { "title": "skills", "info": [{ "title": "Frontend & UI", "icons": [...] }] },
    { "title": "awards", "info": [{ "title": "Best Award", "stage": "2023", ... }] }
  ]
}
```
