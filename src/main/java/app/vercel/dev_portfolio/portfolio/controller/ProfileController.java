package app.vercel.dev_portfolio.portfolio.controller;


import app.vercel.dev_portfolio.portfolio.dto.ProfileResponse;
import app.vercel.dev_portfolio.portfolio.entity.ResumeDownloadLog;
import app.vercel.dev_portfolio.portfolio.repository.DownloadLogRepository;
import app.vercel.dev_portfolio.portfolio.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for Home/Hero section")
public class ProfileController {

    private final ProfileService profileService;
    private final DownloadLogRepository downloadLogRepository;

    @GetMapping("/getProfile")
    @Operation(summary = "Get hero section data", description = "Fetches name, headline, and bio")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfileData());
    }

    @GetMapping("/resume/download")
    @Operation(summary = "Track and download resume")
    public ResponseEntity<Void> downloadResume(HttpServletRequest request) {
        String cloudinaryUrl = profileService.trackAndGetResumeUrl(request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(cloudinaryUrl))
                .build();
    }

    @GetMapping("/resume/stats")
    public ResponseEntity<Map<String, Object>> getDownloadStats() {
        List<ResumeDownloadLog> downloadsInfo = downloadLogRepository.findAll();
        // You could also get unique IPs or latest downloads here
        return ResponseEntity.ok(Map.of("downloads", downloadsInfo));
    }
}
