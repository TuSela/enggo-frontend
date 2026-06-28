package com.nhom12.enggo_backend.dto.request.gamification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BadgeRequest {
    @NotBlank(message = "BADGE_NAME_REQUIRED")
    @Size(max = 100, message = "BADGE_NAME_INVALID")
    String badgeName;

    String description;

}
