package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.PvpMatchRequest;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.service.gamification.PvpMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/pvp-matches")
@RequiredArgsConstructor
public class PvpMatchController {
    private final PvpMatchService pvpMatchService;

    @GetMapping
    ApiResponse<List<PvpMatchResponse>> getPvpMatches() {
        return ApiResponse.<List<PvpMatchResponse>>builder().result(pvpMatchService.getPvpMatches()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<PvpMatchResponse> getPvpMatch(@PathVariable Integer id) {
        return ApiResponse.<PvpMatchResponse>builder().result(pvpMatchService.getPvpMatch(id)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<PvpMatchResponse> updatePvpMatch(@PathVariable Integer id, @RequestBody PvpMatchRequest request) {
        return ApiResponse.<PvpMatchResponse>builder().result(pvpMatchService.updatePvpMatch(id, request)).build();
    }
}
