package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeaderboardItemResponse {
    Integer id;
    String username;
    String avatarUrl;
    Integer elo;
    Integer rank;
}
