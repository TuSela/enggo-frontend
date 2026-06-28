package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.SkillRequest;
import com.nhom12.enggo_backend.entity.exam.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    Skill toSkill(SkillRequest skillRequest);
    void updateSkill(@MappingTarget Skill skill, SkillRequest skillRequest);
}
