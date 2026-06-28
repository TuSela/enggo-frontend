package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.MissionProgressRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.service.gamification.MissionProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/mission-progress")
@RequiredArgsConstructor
public class MissionProgressController {
    private final MissionProgressService missionProgressService;

    @GetMapping
    ApiResponse<List<MissionProgressResponse>> getMissionProgresses() {
        return ApiResponse.<List<MissionProgressResponse>>builder().result(missionProgressService.getMissionProgresses()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<MissionProgressResponse> getMissionProgress(@PathVariable Integer id) {
        return ApiResponse.<MissionProgressResponse>builder().result(missionProgressService.getMissionProgress(id)).build();
    }

    @PostMapping
    ApiResponse<MissionProgressResponse> createMissionProgress(@RequestBody MissionProgressRequest request) {
        return ApiResponse.<MissionProgressResponse>builder().result(missionProgressService.createMissionProgress(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<MissionProgressResponse> updateMissionProgress(@PathVariable Integer id, @RequestBody MissionProgressRequest request) {
        return ApiResponse.<MissionProgressResponse>builder().result(missionProgressService.updateMissionProgress(id, request)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteMissionProgress(@PathVariable Integer id) {
        missionProgressService.deleteMissionProgress(id);
        return ApiResponse.<String>builder().result("Mission progress has been deleted").build();
    }
}
