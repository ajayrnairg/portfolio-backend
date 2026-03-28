package app.vercel.dev_portfolio.portfolio.service;


import app.vercel.dev_portfolio.portfolio.dto.ProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface ProfileService {
    ProfileResponse getProfileData();

    String trackAndGetResumeUrl(HttpServletRequest request);
}
