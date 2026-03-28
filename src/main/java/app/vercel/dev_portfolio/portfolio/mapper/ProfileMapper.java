package app.vercel.dev_portfolio.portfolio.mapper;

import app.vercel.dev_portfolio.portfolio.dto.ProfileResponse;
import app.vercel.dev_portfolio.portfolio.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {
    ProfileResponse toDto(Profile profile);
}