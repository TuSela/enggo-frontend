package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.response.social.FriendResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.social.FriendRequestRepository;
import com.nhom12.enggo_backend.service.social.FriendListService;
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
public class FriendListController {

    FriendListService friendListService;
    UserRepository userRepository;
    FriendRequestRepository friendRequestRepository; // ← THÊM inject

    // Trả về User object thay vì chỉ ID
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    private Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @GetMapping()
    public ApiResponse<List<FriendResponse>> getAllFriends() {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.getAllFriends(getCurrentUserId()))
                .build();
    }

    @GetMapping("/online")
    public ApiResponse<List<FriendResponse>> getOnlineFriends() {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.getOnlineFriends(getCurrentUserId()))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<FriendResponse>> searchFriends(@RequestParam String keyword) {
        return ApiResponse.<List<FriendResponse>>builder()
                .result(friendListService.searchFriends(getCurrentUserId(), keyword))
                .build();
    }

    @GetMapping("/sent-requests")
    public ApiResponse<List<Integer>> getSentRequestIds() {
        User currentUser = getCurrentUser(); // ← dùng getCurrentUser()
        List<Integer> ids = friendRequestRepository.findReceiverIdsBySender(currentUser); // ← chữ thường
        return ApiResponse.<List<Integer>>builder()
                .result(ids)
                .build();
    }
}