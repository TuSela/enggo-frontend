package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LevelInfoResponse {
    Integer currentLevel;
    Integer currentExp;
    Integer nextLevel;
    Integer expGainedInCurrentLevel;
    Integer expRequiredForNextLevel;
    double progressPercentage;
}
