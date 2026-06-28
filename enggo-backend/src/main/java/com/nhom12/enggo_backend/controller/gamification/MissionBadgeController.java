package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.MissionBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionBadgeResponse;
import com.nhom12.enggo_backend.service.gamification.MissionBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/mission-badges")
@RequiredArgsConstructor
public class MissionBadgeController {
    private final MissionBadgeService missionBadgeService;

    @GetMapping
    ApiResponse<List<MissionBadgeResponse>> getMissionBadges() {
        return ApiResponse.<List<MissionBadgeResponse>>builder().result(missionBadgeService.getMissionBadges()).build();
    }

    @GetMapping("/{missionId}/{badgeId}")
    ApiResponse<MissionBadgeResponse> getMissionBadge(@PathVariable Integer missionId, @PathVariable Integer badgeId) {
        return ApiResponse.<MissionBadgeResponse>builder().result(missionBadgeService.getMissionBadge(missionId, badgeId)).build();
    }

    @PostMapping
    ApiResponse<MissionBadgeResponse> createMissionBadge(@RequestBody MissionBadgeRequest request) {
        return ApiResponse.<MissionBadgeResponse>builder().result(missionBadgeService.createMissionBadge(request)).build();
    }

    @PutMapping("/{missionId}/{badgeId}")
    ApiResponse<MissionBadgeResponse> updateMissionBadge(
            @PathVariable Integer missionId,
            @PathVariable Integer badgeId,
            @RequestBody MissionBadgeRequest request
    ) {
        return ApiResponse.<MissionBadgeResponse>builder()
                .result(missionBadgeService.updateMissionBadge(missionId, badgeId, request))
                .build();
    }

    @DeleteMapping("/{missionId}/{badgeId}")
    ApiResponse<String> deleteMissionBadge(@PathVariable Integer missionId, @PathVariable Integer badgeId) {
        missionBadgeService.deleteMissionBadge(missionId, badgeId);
        return ApiResponse.<String>builder().result("Mission badge has been deleted").build();
    }
}
