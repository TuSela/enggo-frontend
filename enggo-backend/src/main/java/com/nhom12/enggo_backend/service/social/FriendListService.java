package com.nhom12.enggo_backend.service.social;

import com.nhom12.enggo_backend.dto.response.social.FriendResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Friend;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.social.FriendRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendListService {
    FriendRepository friendRepository;
    UserRepository userRepository;

    // Lấy toàn bộ danh sách bạn bè
    @Transactional(readOnly = true)
    public List<FriendResponse> getAllFriends(Integer currentUserId) {
        User currentUser = getUser(currentUserId);
        return friendRepository.findAllByUser(currentUser).stream()
                .map(f -> toResponse(f, currentUser))
                .collect(Collectors.toList());
    }

    // Lấy danh sách bạn bè đang online (vòng tròn xanh trên đầu màn hình)
    @Transactional(readOnly = true)
    public List<FriendResponse> getOnlineFriends(Integer currentUserId) {
        User currentUser = getUser(currentUserId);
        return friendRepository.findOnlineFriends(currentUser).stream()
                .map(f -> toResponse(f, currentUser))
                .collect(Collectors.toList());
    }

    // Tìm kiếm bạn bè theo tên
    @Transactional(readOnly = true)
    public List<FriendResponse> searchFriends(Integer currentUserId, String keyword) {
        User currentUser = getUser(currentUserId);
        return friendRepository.searchFriendsByUsername(currentUser, keyword).stream()
                .map(f -> toResponse(f, currentUser))
                // Lọc bỏ chính mình khỏi kết quả tìm kiếm
                .filter(r -> !r.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    // ---- Helper ----

    private User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // Lấy đúng "người kia" trong quan hệ bạn bè
    private User getOtherUser(Friend friend, User currentUser) {
        return friend.getUser1().getId().equals(currentUser.getId())
                ? friend.getUser2()
                : friend.getUser1();
    }

    private FriendResponse toResponse(Friend friend, User currentUser) {
        User other = getOtherUser(friend, currentUser);
        return FriendResponse.builder()
                .userId(other.getId())
                .username(other.getUsername())
                .avatarUrl(other.getAvatarUrl())
                .bio(other.getBio())
                .level(other.getLevel())
                .online("ONLINE".equalsIgnoreCase(other.getStatus()))
                .build();
    }
}
