package app.vercel.dev_portfolio.portfolio.dto;

import java.util.List;

public record ProjectDTO(
        String title,
        String path,
        List<String> techStack,
        String description,
        List<LinkDTO> ctaLinks
) {}