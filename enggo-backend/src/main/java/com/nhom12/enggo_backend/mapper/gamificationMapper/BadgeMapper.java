package com.nhom12.enggo_backend.mapper.gamificationMapper;

import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BadgeMapper {
    BadgeResponse toBadgeResponse(Badge badge);
}
