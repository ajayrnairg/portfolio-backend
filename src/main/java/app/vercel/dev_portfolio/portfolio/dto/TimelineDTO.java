package app.vercel.dev_portfolio.portfolio.dto;

public record TimelineDTO(
        Long id,
        String title,
        String stage,
        String description
) {}