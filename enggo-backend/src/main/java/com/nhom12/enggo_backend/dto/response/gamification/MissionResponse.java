package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MissionResponse {
    Integer id;
    String title;
    String description;
    Integer rewardExp;
    String missionType;
    Integer targetValue;
    String missionKey;
    Integer timeLimitHours;
    String status;
    LocalDateTime createdAt;
}
