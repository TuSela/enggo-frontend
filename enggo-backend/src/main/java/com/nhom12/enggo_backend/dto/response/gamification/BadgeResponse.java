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
public class BadgeResponse {
    Integer id;
    String badgeName;
    String description;
    String iconUrl;
    LocalDateTime createdAt;
}
