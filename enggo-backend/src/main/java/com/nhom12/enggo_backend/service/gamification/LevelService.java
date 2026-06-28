package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.response.gamification.LevelInfoResponse;
import com.nhom12.enggo_backend.entity.gamification.LevelProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelService {
    private final LevelProperties levelProperties;

    public LevelInfoResponse  getLevelInfo(int totalExp){
        List<LevelProperties.LevelConfig> configList = levelProperties.getLevels();

        LevelProperties.LevelConfig currentConfig = configList.getFirst();
        LevelProperties.LevelConfig nextConfig = null;

        for (int i = 0; i < configList.size(); i++) {
            if (totalExp >= configList.get(i).getReqExp()) {
                currentConfig = configList.get(i);

                if (i <  configList.size() - 1) {
                    nextConfig = configList.get(i + 1);
                }
            } else break;
        }

        Integer currentLevel = currentConfig.getLevel();
        Integer minExp = currentConfig.getReqExp();

        Integer nextLevel;
        Integer expGainedInLevel;
        Integer expRequiredInLevel;
        double progressPercentage;

        if (nextConfig == null) {
            nextLevel = currentLevel;
            expGainedInLevel = 100;
            expRequiredInLevel = 100;
            progressPercentage = 100;
        } else {
            nextLevel = nextConfig.getLevel();
            Integer maxExp = nextConfig.getReqExp();

            expGainedInLevel = totalExp - minExp;
            expRequiredInLevel = maxExp - expGainedInLevel;

            double progress = ((double) expGainedInLevel / expRequiredInLevel) * 100;
            progressPercentage = (double) Math.round(progress * 100) / 100;
        }

        return LevelInfoResponse.builder()
                .currentLevel(currentLevel)
                .currentExp(totalExp)
                .nextLevel(nextLevel)
                .progressPercentage(progressPercentage)
                .expRequiredForNextLevel(expRequiredInLevel)
                .expGainedInCurrentLevel(expGainedInLevel)
                .build();
    }
}
