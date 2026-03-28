package app.vercel.dev_portfolio.portfolio.service.impl;



import app.vercel.dev_portfolio.portfolio.dto.ProfileResponse;
import app.vercel.dev_portfolio.portfolio.entity.ResumeDownloadLog;
import app.vercel.dev_portfolio.portfolio.mapper.ProfileMapper;
import app.vercel.dev_portfolio.portfolio.repository.DownloadLogRepository;
import app.vercel.dev_portfolio.portfolio.repository.ProfileRepository;
import app.vercel.dev_portfolio.portfolio.service.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository repository;
    private final ProfileMapper mapper;
    private final DownloadLogRepository downloadLogRepository;

    @Value("${app.cloudinary.resume-url}")
    private String resumeUrl;

    @Override
    public ProfileResponse getProfileData() {
        // Since it's a portfolio, we usually only have 1 profile record (ID: 1)
        return repository.findById(1L)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Profile record not found in DB"));
    }

    @Transactional
    public String trackAndGetResumeUrl(HttpServletRequest request) {
        // 1. Log the download
        ResumeDownloadLog log = new ResumeDownloadLog();
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDownloadedAt(LocalDateTime.now());
        downloadLogRepository.save(log);

        // 2. Return the Cloudinary URL (In a real app, fetch this from the Profile entity)
        return resumeUrl;
    }
}
