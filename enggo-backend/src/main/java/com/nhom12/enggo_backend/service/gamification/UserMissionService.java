package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.dto.response.gamification.ClaimRewardResponse;
import com.nhom12.enggo_backend.entity.gamification.*;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service handling daily missions for a specific user.
 * Provides methods to fetch today's missions, update progress and claim rewards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserMissionService {

    private final MissionRepository missionRepository;
    private final MissionProgressRepository missionProgressRepository;
    private final UserRepository userRepository;

    /**
     * Retrieve today's missions with progress for the given user.
     */
    @Transactional(readOnly = true)
    public List<MissionProgressResponse> getTodayMissions(Integer userId) {
        User user = findUser(userId);
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);
        List<MissionProgress> progresses = missionProgressRepository
                .findByUserIdAndDeadlineBetween(user.getId(), start, end);
        return progresses.stream().map(this::toProgressResponse).collect(Collectors.toList());
    }
    private MissionResponse toMissionResponse(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .rewardExp(mission.getRewardExp())
                .missionType(mission.getMissionType())
                .targetValue(mission.getTargetValue())
                .missionKey(mission.getMissionKey())
                .timeLimitHours(mission.getTimeLimitHours())
                .status(mission.getStatus())
                .createdAt(mission.getCreatedAt())
                .build();
    }
    /**
     * Tăng tiến độ mission một cách "an toàn": chạy trong transaction riêng
     * (REQUIRES_NEW) và tự bắt lỗi, để một mission lỗi (vd đã CLAIMED, hoặc
     * race condition) không bao giờ làm rollback transaction của nơi gọi
     * (ví dụ submitExam, finalizeMatch — vốn chứa các thay đổi quan trọng
     * như exp, level, elo, streakDays).
     *
     * Lưu ý: phải gọi qua bean UserMissionService (cross-class call) để
     * Spring AOP proxy áp dụng đúng @Transactional; self-invocation trong
     * cùng class sẽ bỏ qua annotation này.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void incrementProgressSafely(Integer userId, Integer missionId, int increment) {
        try {
            incrementProgress(userId, missionId, increment);
        } catch (Exception e) {
            log.warn("Failed to increment mission {} for user {}: {}", missionId, userId, e.getMessage());
        }
    }

    /**
     * Increment progress for a mission. If the target is reached, status becomes COMPLETED.
     */
    @Transactional
    public MissionProgressResponse incrementProgress(Integer userId, Integer missionId, int increment) {
        MissionProgress progress = findProgress(userId, missionId);
        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }
        int newValue = (progress.getCurrentValue() == null ? 0 : progress.getCurrentValue()) + increment;
        progress.setCurrentValue(newValue);
        // If target reached, mark as COMPLETED
        if (progress.getMission().getTargetValue() != null && newValue >= progress.getMission().getTargetValue()) {
            progress.setStatus("COMPLETED");
        }
        MissionProgress saved = missionProgressRepository.save(progress);
        return toProgressResponse(saved);
    }

    /**
     * Claim reward for a completed mission.
     */
    private final BadgeRepository badgeRepository;
    private final MissionBadgeRepository missionBadgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Transactional
    public ClaimRewardResponse claimReward(Integer userId, Integer missionId) {
        MissionProgress progress = findProgress(userId, missionId);

        // 1. Kiểm tra tiến độ hoàn thành nhiệm vụ
        if (progress.getMission().getTargetValue() > progress.getCurrentValue()) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        // 2. Kiểm tra xem nhiệm vụ đã được nhận thưởng trước đó chưa
        if ("CLAIMED".equals(progress.getStatus())) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        // 3. Cộng kinh nghiệm (EXP) cho User
        User user = progress.getUser();
        int reward = progress.getMission().getRewardExp() != null ? progress.getMission().getRewardExp() : 0;
        user.addExp(reward);
        userRepository.save(user);

        // 4. Cập nhật trạng thái tiến độ thành CLAIMED
        progress.setStatus("CLAIMED");
        missionProgressRepository.save(progress);

        Mission mission = progress.getMission();
        List<MissionBadge> missionBadges = missionBadgeRepository.findByMission(mission);

        // Khởi tạo danh sách chứa các Badge mà User THỰC SỰ nhận được trong lượt bấm này
        List<Badge> earnedBadges = new ArrayList<>();

        if (missionBadges != null && !missionBadges.isEmpty()) {
            for (MissionBadge mb : missionBadges) {
                Badge badge = mb.getBadge();
                if (badge != null) {
                    UserBadgeId userBadgeId = new UserBadgeId(user.getId(), badge.getId());
                    boolean isAlreadyOwned = userBadgeRepository.existsById(userBadgeId);

                    if (!isAlreadyOwned) {
                        UserBadge userBadge = UserBadge.builder()
                                .id(userBadgeId)
                                .user(user)
                                .badge(badge)
                                .build();

                        userBadgeRepository.save(userBadge);

                        // Gom Huy hiệu mới nhận vào danh sách
                        earnedBadges.add(badge);
                    }
                }
            }
        }

        // 6. SỬA TẠI ĐÂY: Ánh xạ danh sách Badge sang BadgeResponse bằng Stream API
        // (Nếu bạn có badgeMapper, có thể thay thế bằng: badgeMapper.toBadgeResponseList(earnedBadges))
        List<BadgeResponse> badgeResponseList = earnedBadges.stream()
                .map(badge -> BadgeResponse.builder()
                        .id(badge.getId())
                        .badgeName(badge.getBadgeName())
                        .description(badge.getDescription())
                        .iconUrl(badge.getIconUrl())
                        // Gán thêm các trường khác của BadgeResponse nếu DTO của bạn có yêu cầu
                        .build())
                .toList();

        // 7. Trả về kết quả hoàn chỉnh chứa danh sách Huy hiệu vừa đạt được
        return ClaimRewardResponse.builder()
                .expAwarded(reward)
                .newTotalExp(user.getExp())
                .status("CLAIMED")
                .badgeResponse(badgeResponseList) // Bắn dữ liệu Badge ra đây
                .build();
    }

    // Helper methods -----------------------------------------------------
    private User findUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionProgress findProgress(Integer userId, Integer missionId) {
        return missionProgressRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MissionProgressResponse toProgressResponse(MissionProgress progress) {
        return MissionProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .username(progress.getUser().getUsername())
                .missionResponse(toMissionResponse(progress.getMission()))
                .currentValue(progress.getCurrentValue())
                .status(progress.getStatus())
                .deadline(progress.getDeadline())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
