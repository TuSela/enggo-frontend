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
public class MissionRequest {
    String title;
    String description;
    Integer rewardExp;
    String missionType;
    Integer targetValue;
    String missionKey;
    Integer timeLimitHours;
    String status;
}
