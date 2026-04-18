package app.vercel.dev_portfolio.portfolio.dto;

public record ProfileResponse(
        String name,
        String headline,
        String subHeadline,
        String resumeUrl,
        String email,
        String linkedinUrl,
        String githubUrl,
        String youtubeUrl,
        String instagramUrl,
        String facebookUrl
) {}
