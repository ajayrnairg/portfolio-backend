package app.vercel.dev_portfolio.portfolio.dto;

import java.util.List;

public record AboutFullResponse(
        AboutLeftResponse leftSection,
        List<AboutTabDTO> tabs
) {}