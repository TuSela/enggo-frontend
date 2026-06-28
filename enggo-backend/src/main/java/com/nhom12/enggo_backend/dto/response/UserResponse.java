package com.nhom12.enggo_backend.dto.response;

import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer id;
    String username;
    String fullName;
    String email;
    Integer exp;
    Integer level;
    Integer streakDays;
    Integer completedTasks;
    Integer pvpWins;
    String avatarUrl;
    String status;
    String bio;
    Integer elo;
    LocalDateTime createdAt;
    Integer winStreak;
    Set<RoleResponse> roles;
    BadgeResponse badgeRank;
    Integer leaderboardRank;
}
