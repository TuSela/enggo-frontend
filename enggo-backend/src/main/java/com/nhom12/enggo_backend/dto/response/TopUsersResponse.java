package com.nhom12.enggo_backend.dto.response;

import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import java.util.List;

/**
 * Response object containing the top users by elo and the elo of the current authenticated user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopUsersResponse {
    // List of the top 4 users ordered by elo descending
    List<UserResponse> topUsers;
    UserResponse myRank;

}
