package app.vercel.dev_portfolio.portfolio.dto;

import java.util.List;

public record AboutTabDTO(
        String title, // "skills", "awards", etc.
        List<?> info // This handles the different shapes of SkillCategory vs Experience
) {}