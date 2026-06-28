package com.nhom12.enggo_backend.dto.response.gamification;

import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteResponse {
    int inviteId;
    Integer inviterPlayerId;
    Integer inviteePlayerId;
    String inviterUsername;
    String inviteeUsername;
    RandomBlueprintRequest randomBlueprintRequest;
    String status; // PENDING, ACCEPTED, DECLINED, TIMEOUT
}
