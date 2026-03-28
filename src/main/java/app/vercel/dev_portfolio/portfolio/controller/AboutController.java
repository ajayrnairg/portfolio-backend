package app.vercel.dev_portfolio.portfolio.controller;

import app.vercel.dev_portfolio.portfolio.dto.AboutFullResponse;
import app.vercel.dev_portfolio.portfolio.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping
    public ResponseEntity<AboutFullResponse> getAboutData() {
        return ResponseEntity.ok(aboutService.getFullAboutData());
    }
}
