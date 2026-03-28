package app.vercel.dev_portfolio.portfolio.controller;

import app.vercel.dev_portfolio.portfolio.dto.WorkSectionResponse;
import app.vercel.dev_portfolio.portfolio.service.WorkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work")
@RequiredArgsConstructor
@Tag(name = "Work", description = "Endpoints for Featured Engineering/Projects section")
public class WorkController {

    private final WorkService workService;

    @GetMapping
    public ResponseEntity<WorkSectionResponse> getWorkData() {
        return ResponseEntity.ok(workService.getWorkData());
    }
}
