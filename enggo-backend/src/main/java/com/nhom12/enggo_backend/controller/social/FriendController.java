package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.UserResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.social.FriendService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/social/friends")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendService friendService;
    UserRepository userRepository;

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Kh�ng t�m th?y th�ng tin t�i kho?n ngu?i d�ng"));
            return user.getId();
        }
        throw new RuntimeException("T�i kho?n chua du?c x�c th?c ho?c phi�n dang nh?p h?t h?n");
    }

    // 0. API L?y danh s�ch b?n b�
    @GetMapping("/pvp")
    public ApiResponse<List<UserResponse>> getFriends() {
        Integer currentUserId = getCurrentUserId();
        return ApiResponse.<List<UserResponse>>builder()
                .result(friendService.getFriends(currentUserId))
                .build();
    }
    // 1. API G?i l?i m?i k?t b?n
    @PostMapping("/request/{receiverId}")
    public ApiResponse<Boolean> sendFriendRequest(@PathVariable Integer receiverId) {
        Integer currentUserId = getCurrentUserId();
        friendService.sendFriendRequest(currentUserId, receiverId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // 2. API Ch?p nh?n l?i m?i k?t b?n
    @PostMapping("/accept/{requestId}")
    public ApiResponse<Boolean> acceptFriendRequest(@PathVariable Integer requestId) {
        Integer currentUserId = getCurrentUserId();
        friendService.acceptFriendRequest(requestId, currentUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // 3. API T? ch?i l?i m?i k?t b?n
    @DeleteMapping("/reject/{requestId}")
    public ApiResponse<Boolean> rejectFriendRequest(@PathVariable Integer requestId) {
        Integer currentUserId = getCurrentUserId();
        friendService.rejectFriendRequest(requestId, currentUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }
    // 4. API H?y k?t b?n
    @DeleteMapping("/unfriend/{targetUserId}")
    public ApiResponse<Boolean> unfriend(@PathVariable Integer targetUserId) {
        Integer currentUserId = getCurrentUserId();
        friendService.unfriend(currentUserId, targetUserId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }
}
