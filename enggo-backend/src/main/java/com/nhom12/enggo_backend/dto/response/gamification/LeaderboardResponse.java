package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LeaderboardResponse {
    List<LeaderboardItemResponse> topPlayers;
    LeaderboardItemResponse currentUser;
}
