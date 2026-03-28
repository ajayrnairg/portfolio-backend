package app.vercel.dev_portfolio.portfolio.dto;

public record AboutLeftResponse(
        String title,
        String description,
        String yearsExperience,
        String projectsCompleted,
        String techDebtReduced
) {}
