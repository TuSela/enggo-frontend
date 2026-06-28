package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.MissionRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.service.gamification.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/missions")
@RequiredArgsConstructor
public class MissionController {
    private final MissionService missionService;

    @GetMapping
    ApiResponse<List<MissionResponse>> getMissions() {
        return ApiResponse.<List<MissionResponse>>builder().result(missionService.getMissions()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<MissionResponse> getMission(@PathVariable Integer id) {
        return ApiResponse.<MissionResponse>builder().result(missionService.getMission(id)).build();
    }

    @PostMapping
    ApiResponse<MissionResponse> createMission(@RequestBody MissionRequest request) {
        return ApiResponse.<MissionResponse>builder().result(missionService.createMission(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<MissionResponse> updateMission(@PathVariable Integer id, @RequestBody MissionRequest request) {
        return ApiResponse.<MissionResponse>builder().result(missionService.updateMission(id, request)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteMission(@PathVariable Integer id) {
        missionService.deleteMission(id);
        return ApiResponse.<String>builder().result("Mission has been deleted").build();
    }
}
