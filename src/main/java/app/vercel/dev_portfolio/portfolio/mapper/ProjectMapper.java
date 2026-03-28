package app.vercel.dev_portfolio.portfolio.mapper;

import app.vercel.dev_portfolio.portfolio.dto.LinkDTO;
import app.vercel.dev_portfolio.portfolio.dto.ProjectDTO;
import app.vercel.dev_portfolio.portfolio.entity.Project;
import app.vercel.dev_portfolio.portfolio.entity.ProjectLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper {
    @Mapping(target = "path", source = "thumbnailPath")
    ProjectDTO toDto(Project project);

    @Mapping(target = "title", source = "label")
    @Mapping(target = "icon", source = "iconName")
    LinkDTO toLinkDto(ProjectLink link);
}