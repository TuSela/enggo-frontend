package com.nhom12.enggo_backend.dto.request.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BadgeEventRequest {
    Integer badgeId;
    Integer eventId;
    Integer minScoreRequired;
    Integer rewardExp;
}
