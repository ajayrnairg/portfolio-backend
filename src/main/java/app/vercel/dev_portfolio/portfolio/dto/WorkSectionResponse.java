package app.vercel.dev_portfolio.portfolio.dto;

import java.util.List;

public record WorkSectionResponse(
        String title,
        String description,
        List<ProjectDTO> projects
) {}