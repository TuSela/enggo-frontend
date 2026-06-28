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
public class MissionProgressResponse {
    Integer id;
    Integer userId;
    String username;
    MissionResponse missionResponse;
    Integer currentValue;
    String status;
    LocalDateTime deadline;
    LocalDateTime updatedAt;
}
