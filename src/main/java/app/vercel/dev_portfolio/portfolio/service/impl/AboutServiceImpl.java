package app.vercel.dev_portfolio.portfolio.service.impl;

import app.vercel.dev_portfolio.portfolio.dto.AboutFullResponse;
import app.vercel.dev_portfolio.portfolio.dto.AboutTabDTO;
import app.vercel.dev_portfolio.portfolio.mapper.AboutMapper;
import app.vercel.dev_portfolio.portfolio.repository.*;
import app.vercel.dev_portfolio.portfolio.service.AboutService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {
    private final AboutSectionRepository aboutRepo;
    private final SkillCategoryRepository skillRepo;
    private final AwardRepository awardRepo;
    private final ExperienceRepository expRepo;
    private final CertificationRepository certRepo;
    private final AboutMapper mapper;

    @Override
    public AboutFullResponse getFullAboutData() {
        var left = aboutRepo.findById(1L).orElseThrow(() -> new EntityNotFoundException("About section missing"));

        List<AboutTabDTO> tabs = List.of(
                new AboutTabDTO("skills", skillRepo.findAllWithSkills().stream().map(mapper::toSkillCategoryDto).toList()),
                new AboutTabDTO("awards", awardRepo.findAll().stream().map(mapper::awardToDto).toList()),
                new AboutTabDTO("experience", expRepo.findAll().stream().map(mapper::expToDto).toList()),
                new AboutTabDTO("certifications", certRepo.findAll().stream().map(mapper::certToDto).toList())
        );

        return new AboutFullResponse(mapper.toLeftDto(left), tabs);
    }
}