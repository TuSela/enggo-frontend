package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.ThemeRequest;
import com.nhom12.enggo_backend.dto.response.exam.ThemeResponse;
import com.nhom12.enggo_backend.entity.exam.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.web.multipart.MultipartFile;


@Mapper(componentModel = "spring")
public interface ThemeMapper {
    @Mapping(target = "themeImage", ignore = true)
    Theme toTheme(ThemeRequest theme);
    @Mapping(source = "themeImage", target = "imageUrl")
    ThemeResponse toThemeResponse(Theme theme);
    @Mapping(target = "themeImage", ignore = true)
    void updateTheme(@MappingTarget Theme theme, ThemeRequest themeRequest);
}
