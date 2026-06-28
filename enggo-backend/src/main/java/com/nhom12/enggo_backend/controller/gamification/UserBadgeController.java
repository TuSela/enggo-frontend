package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.UserBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.UserBadgeResponse;
import com.nhom12.enggo_backend.service.gamification.UserBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {
    private final UserBadgeService userBadgeService;

    @GetMapping("/my-badges")
    ApiResponse<List<UserBadgeResponse>> getMyBadges() {
        return ApiResponse.<List<UserBadgeResponse>>builder().result(userBadgeService.getMyBadges()).build();
    }

    @GetMapping
    ApiResponse<List<UserBadgeResponse>> getUserBadges() {
        return ApiResponse.<List<UserBadgeResponse>>builder().result(userBadgeService.getUserBadges()).build();
    }

    @GetMapping("/{userId}/{badgeId}")
    ApiResponse<UserBadgeResponse> getUserBadge(@PathVariable Integer userId, @PathVariable Integer badgeId) {
        return ApiResponse.<UserBadgeResponse>builder().result(userBadgeService.getUserBadge(userId, badgeId)).build();
    }

    @PostMapping
    ApiResponse<UserBadgeResponse> createUserBadge(@RequestBody UserBadgeRequest request) {
        return ApiResponse.<UserBadgeResponse>builder().result(userBadgeService.createUserBadge(request)).build();
    }

    @PutMapping("/{userId}/{badgeId}")
    ApiResponse<UserBadgeResponse> updateUserBadge(
            @PathVariable Integer userId,
            @PathVariable Integer badgeId,
            @RequestBody UserBadgeRequest request
    ) {
        return ApiResponse.<UserBadgeResponse>builder()
                .result(userBadgeService.updateUserBadge(userId, badgeId, request))
                .build();
    }

    @DeleteMapping("/{userId}/{badgeId}")
    ApiResponse<String> deleteUserBadge(@PathVariable Integer userId, @PathVariable Integer badgeId) {
        userBadgeService.deleteUserBadge(userId, badgeId);
        return ApiResponse.<String>builder().result("User badge has been deleted").build();
    }
}
