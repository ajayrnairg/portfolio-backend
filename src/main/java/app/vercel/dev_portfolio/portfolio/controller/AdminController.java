package app.vercel.dev_portfolio.portfolio.controller;

import app.vercel.dev_portfolio.portfolio.entity.*;
import app.vercel.dev_portfolio.portfolio.repository.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Management endpoints for all sections")
@SecurityRequirement(name = "bearerAuth") // For Swagger documentation
public class AdminController {

    private final ProfileRepository profileRepository;
    private final AboutSectionRepository aboutSectionRepository;
    private final ExperienceRepository experienceRepository;
    private final AwardRepository awardRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;

    // --- Profile Management ---
    @PutMapping("/profile")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
        profile.setId(1L); // Ensure we only ever update the single profile record
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

    @DeleteMapping("/certifications/{id}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id) {
        certificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
