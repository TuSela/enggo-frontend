package com.nhom12.enggo_backend.dto.request.gamification;

import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteRequest {
    String inviteeUsername;
    RandomBlueprintRequest randomBlueprintRequest;
}
