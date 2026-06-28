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
public class EventResponse {
    Integer id;
    String eventName;
    String description;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Integer requiredLevel;
    String status;
    String eventType;
    String bannerUrl;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
