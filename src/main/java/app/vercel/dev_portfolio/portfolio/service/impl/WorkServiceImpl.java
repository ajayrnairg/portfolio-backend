package app.vercel.dev_portfolio.portfolio.service.impl;

import app.vercel.dev_portfolio.portfolio.dto.WorkSectionResponse;
import app.vercel.dev_portfolio.portfolio.mapper.ProjectMapper;
import app.vercel.dev_portfolio.portfolio.repository.ProjectRepository;
import app.vercel.dev_portfolio.portfolio.repository.WorkSectionRepository;
import app.vercel.dev_portfolio.portfolio.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    private final ProjectRepository projectRepository;
    private final WorkSectionRepository sectionRepository;
    private final ProjectMapper mapper;

    public WorkSectionResponse getWorkData() {
        var section = sectionRepository.findById(1L).orElseThrow();
        var projects = projectRepository.findAll(); // Add sorting if needed

        return new WorkSectionResponse(
                section.getTitle(),
                section.getDescription(),
                projects.stream().map(mapper::toDto).toList()
        );
    }
}