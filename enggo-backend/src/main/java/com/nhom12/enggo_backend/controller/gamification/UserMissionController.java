package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.gamification.ClaimRewardResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.service.gamification.UserMissionService;
import com.nhom12.enggo_backend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gamification/missions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMissionController {

    UserMissionService userMissionService;
    UserService userService; // to reuse method for getting current user if needed

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.getMyInfo().getId(); // getMyInfo returns UserResponse which contains id
    }
    @GetMapping("/today")
    public ApiResponse<List<MissionProgressResponse>> getTodayMissions() {
        Integer userId = getCurrentUserId();
        List<MissionProgressResponse> missions = userMissionService.getTodayMissions(userId);
        return ApiResponse.<List<MissionProgressResponse>>builder().result(missions).build();
    }
    @PostMapping("/{missionId}/progress")
    public ApiResponse<MissionProgressResponse> incrementProgress(@PathVariable Integer missionId,
                                                                   @RequestBody(required = false) Map<String, Integer> body) {
        Integer inc = 1;
        if (body != null && body.containsKey("increment")) {
            inc = body.get("increment");
        }
        Integer userId = getCurrentUserId();
        MissionProgressResponse updated = userMissionService.incrementProgress(userId, missionId, inc);
        return ApiResponse.<MissionProgressResponse>builder().result(updated).build();
    }
    @PostMapping("/{missionId}/claim")
    public ApiResponse<ClaimRewardResponse> claimReward(@PathVariable Integer missionId) {
        Integer userId = getCurrentUserId();
        ClaimRewardResponse response = userMissionService.claimReward(userId, missionId);
        return ApiResponse.<ClaimRewardResponse>builder().result(response).build();
    }
}
