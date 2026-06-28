package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Response returned after a user successfully claims a mission reward.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClaimRewardResponse {
    /** Amount of experience awarded for the mission */
    Integer expAwarded;
    /** User's total experience after the award */
    Integer newTotalExp;
    /** New status of the mission progress (always CLAIMED) */
    String status;

    List<BadgeResponse> badgeResponse;
}
