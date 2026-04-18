package app.vercel.dev_portfolio.portfolio.controller;

import app.vercel.dev_portfolio.portfolio.entity.*;
import app.vercel.dev_portfolio.portfolio.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Management endpoints for all sections")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ProfileRepository profileRepository;
    private final AboutSectionRepository aboutSectionRepository;
    private final ExperienceRepository experienceRepository;
    private final AwardRepository awardRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillRepository skillRepository;
    private final ContactRepository contactRepository;

    // --- Profile Management ---
    @PutMapping("/profile")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
        profile.setId(1L);
        return ResponseEntity.ok(profileRepository.save(profile));
    }

    // --- About Section Management ---
    @PutMapping("/about/section")
    public ResponseEntity<AboutSection> updateAboutSection(@RequestBody AboutSection section) {
        section.setId(1L);
        return ResponseEntity.ok(aboutSectionRepository.save(section));
    }

    // --- Experience Management ---
    @PostMapping("/experience")
    public ResponseEntity<Experience> addExperience(@RequestBody Experience exp) {
        return ResponseEntity.ok(experienceRepository.save(exp));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<Experience> updateExperience(@PathVariable Long id, @RequestBody Experience exp) {
        exp.setId(id);
        return ResponseEntity.ok(experienceRepository.save(exp));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        experienceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Projects Management ---
    @PostMapping("/projects")
    public ResponseEntity<Project> addProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Awards Management ---
    @PostMapping("/awards")
    public ResponseEntity<Awards> addAward(@RequestBody Awards award) {
        return ResponseEntity.ok(awardRepository.save(award));
    }

    @PutMapping("/awards/{id}")
    public ResponseEntity<Awards> updateAward(@PathVariable Long id, @RequestBody Awards award) {
        award.setId(id);
        return ResponseEntity.ok(awardRepository.save(award));
    }

    @DeleteMapping("/awards/{id}")
    public ResponseEntity<Void> deleteAward(@PathVariable Long id) {
        awardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Certifications Management ---
    @PostMapping("/certifications")
    public ResponseEntity<Certifications> addCertification(@RequestBody Certifications certification) {
        return ResponseEntity.ok(certificationRepository.save(certification));
    }

    @PutMapping("/certifications/{id}")
    public ResponseEntity<Certifications> updateCertification(@PathVariable Long id, @RequestBody Certifications certification) {
        certification.setId(id);
        return ResponseEntity.ok(certificationRepository.save(certification));
    }

    @DeleteMapping("/certifications/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Skills Management ---
    @PostMapping("/skill-categories")
    public ResponseEntity<SkillCategory> addSkillCategory(@RequestBody SkillCategory category) {
        return ResponseEntity.ok(skillCategoryRepository.save(category));
    }

    @PutMapping("/skill-categories/{id}")
    public ResponseEntity<SkillCategory> updateSkillCategory(@PathVariable Long id, @RequestBody SkillCategory category) {
        category.setId(id);
        return ResponseEntity.ok(skillCategoryRepository.save(category));
    }

    @DeleteMapping("/skill-categories/{id}")
    public ResponseEntity<Void> deleteSkillCategory(@PathVariable Long id) {
        skillCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/skill-categories/{categoryId}/skills")
    public ResponseEntity<Skill> addSkill(@PathVariable Long categoryId, @RequestBody Skill skill) {
        SkillCategory category = skillCategoryRepository.findById(categoryId)
                 .orElseThrow(() -> new RuntimeException("Category not found"));
        skill.setCategory(category);
        return ResponseEntity.ok(skillRepository.save(skill));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        Skill existing = skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));
        skill.setId(id);
        skill.setCategory(existing.getCategory());
        return ResponseEntity.ok(skillRepository.save(skill));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Contact Management ---
    @GetMapping("/contact-submissions")
    public ResponseEntity<List<ContactMessage>> getContactSubmissions() {
        return ResponseEntity.ok(contactRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/contact-submissions/bulk-delete")
    public ResponseEntity<Void> deleteContactSubmissions(@RequestBody List<Long> ids) {
        contactRepository.deleteAllById(ids);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile/contact-info")
    public ResponseEntity<Profile> updateContactInfo(@RequestBody Map<String, String> contactUpdates) {
        Profile p = profileRepository.findById(1L).orElseThrow(() -> new RuntimeException("Profile not found"));
        if (contactUpdates.containsKey("email")) p.setEmail(contactUpdates.get("email"));
        if (contactUpdates.containsKey("githubUrl")) p.setGithubUrl(contactUpdates.get("githubUrl"));
        if (contactUpdates.containsKey("linkedinUrl")) p.setLinkedinUrl(contactUpdates.get("linkedinUrl"));
        if (contactUpdates.containsKey("youtubeUrl")) p.setYoutubeUrl(contactUpdates.get("youtubeUrl"));
        if (contactUpdates.containsKey("instagramUrl")) p.setInstagramUrl(contactUpdates.get("instagramUrl"));
        if (contactUpdates.containsKey("facebookUrl")) p.setFacebookUrl(contactUpdates.get("facebookUrl"));
        return ResponseEntity.ok(profileRepository.save(p));
    }
}
