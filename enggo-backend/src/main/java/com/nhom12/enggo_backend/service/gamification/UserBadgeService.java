package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.UserBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.UserBadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.gamification.UserBadge;
import com.nhom12.enggo_backend.entity.gamification.UserBadgeId;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBadgeService {
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getMyBadges() {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userBadgeRepository.findAllByUser_Username(username).stream()
                .map(this::toUserBadgeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getUserBadges() {
        return userBadgeRepository.findAll().stream().map(this::toUserBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserBadgeResponse getUserBadge(Integer userId, Integer badgeId) {
        return toUserBadgeResponse(findUserBadge(userId, badgeId));
    }

    public UserBadgeResponse createUserBadge(UserBadgeRequest request) {
        UserBadgeId id = new UserBadgeId(request.getUserId(), request.getBadgeId());
        if (userBadgeRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        UserBadge userBadge = UserBadge.builder()
                .id(id)
                .user(findUser(request.getUserId()))
                .badge(findBadge(request.getBadgeId()))
                .build();
        return toUserBadgeResponse(userBadgeRepository.save(userBadge));
    }

    public UserBadgeResponse updateUserBadge(Integer userId, Integer badgeId, UserBadgeRequest request) {
        UserBadge userBadge = findUserBadge(userId, badgeId);
        if (!userId.equals(request.getUserId()) || !badgeId.equals(request.getBadgeId())) {
            userBadgeRepository.delete(userBadge);
            return createUserBadge(request);
        }
        return toUserBadgeResponse(userBadge);
    }

    public void deleteUserBadge(Integer userId, Integer badgeId) {
        userBadgeRepository.delete(findUserBadge(userId, badgeId));
    }

    private UserBadgeResponse toUserBadgeResponse(UserBadge userBadge) {
        return UserBadgeResponse.builder()
                .userId(userBadge.getUser().getId())
                .username(userBadge.getUser().getUsername())
                .badgeId(userBadge.getBadge().getId())
                .badgeName(userBadge.getBadge().getBadgeName())
                .description(userBadge.getBadge().getDescription())
                .iconUrl(userBadge.getBadge().getIconUrl())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }

    private UserBadge findUserBadge(Integer userId, Integer badgeId) {
        return userBadgeRepository.findById(new UserBadgeId(userId, badgeId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Badge findBadge(Integer id) {
        return badgeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
