package app.vercel.dev_portfolio.portfolio.mapper;

import app.vercel.dev_portfolio.portfolio.dto.AboutLeftResponse;
import app.vercel.dev_portfolio.portfolio.dto.SkillCategoryDTO;
import app.vercel.dev_portfolio.portfolio.dto.SkillIconDTO;
import app.vercel.dev_portfolio.portfolio.dto.TimelineDTO;
import app.vercel.dev_portfolio.portfolio.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AboutMapper {
    AboutLeftResponse toLeftDto(AboutSection entity);

    TimelineDTO awardToDto(Awards entity);
    TimelineDTO expToDto(Experience entity);
    TimelineDTO certToDto(Certifications entity);

    @Mapping(target = "title", source = "categoryName")
    @Mapping(target = "icons", source = "skills")
    SkillCategoryDTO toSkillCategoryDto(SkillCategory entity);

    @Mapping(target = "icon", source = "iconName")
    @Mapping(target = "title", source = "skillName")
    SkillIconDTO toSkillIconDto(Skill entity);
}
