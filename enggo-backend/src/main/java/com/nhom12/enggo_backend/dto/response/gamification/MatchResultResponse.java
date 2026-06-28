package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchResultResponse {
    Integer matchId;
    Integer winnerId;
    Integer player1Id;
    Integer player2Id;
    PlayerResult player1;
    PlayerResult player2;
    String status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PlayerResult {
        String userName;
        Integer level;
        String avatarUrl;
        Integer playerScore;
        Integer eloChange;
        Integer correctAnswersCount;
        Integer totalQuestions;
        String duration;
        Integer WinStreak;
        Integer elo;
        BadgeResponse badgeRank;
    }
}