package app.vercel.dev_portfolio.portfolio.dto;

import java.util.List;

public record SkillCategoryDTO(
        String title, // "Frontend & UI"
        List<SkillIconDTO> icons
) {}
