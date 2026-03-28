package app.vercel.dev_portfolio.portfolio.controller;

import app.vercel.dev_portfolio.portfolio.dto.ContactRequest;
import app.vercel.dev_portfolio.portfolio.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService service;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendMessage(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        service.processMessage(request, httpRequest.getRemoteAddr());

        return ResponseEntity.ok(Map.of("message", "Talk to you soon, Ajay!"));
    }
}
