package com.nhom12.enggo_backend.dto.request.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    String eventName;
    String description;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Integer requiredLevel;
    String status;
    String eventType;
    String bannerUrl;
}
