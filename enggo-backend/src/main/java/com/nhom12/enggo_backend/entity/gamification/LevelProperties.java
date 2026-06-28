package com.nhom12.enggo_backend.entity.gamification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "game")
@Data
public class LevelProperties {
    List<LevelConfig> levels;

    @Data
    public static class LevelConfig {
        private Integer level;
        private Integer reqExp;
    }
}
