package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.response.gamification.ClaimRewardResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserMissionServiceTest {

    @Mock
    private MissionRepository missionRepository;
    @Mock
    private MissionProgressRepository missionProgressRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserMissionService userMissionService;

    private User user;
    private Mission mission;
    private MissionProgress progress;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1).username("testUser").exp(100).build();
        mission = Mission.builder().id(10).title("Test Mission").targetValue(5).rewardExp(20).build();
        progress = MissionProgress.builder()
                .id(100)
                .user(user)
                .mission(mission)
                .currentValue(0)
                .status("IN_PROGRESS")
                .deadline(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void claimReward_successful() {
        when(missionProgressRepository.findByUserIdAndMissionId(user.getId(), mission.getId()))
                .thenReturn(Optional.of(progress));
        // Simulate that progress is already completed
        progress.setStatus("COMPLETED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(missionProgressRepository.save(any(MissionProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimRewardResponse resp = userMissionService.claimReward(user.getId(), mission.getId());
        assertEquals(20, resp.getExpAwarded());
        assertEquals(120, resp.getNewTotalExp());
        assertEquals("CLAIMED", resp.getStatus());
        assertEquals("CLAIMED", progress.getStatus());
        verify(userRepository).save(user);
        verify(missionProgressRepository).save(progress);
    }

    @Test
    void claimReward_notCompleted_shouldThrow() {
        when(missionProgressRepository.findByUserIdAndMissionId(user.getId(), mission.getId()))
                .thenReturn(Optional.of(progress));
        // progress still IN_PROGRESS
        AppException ex = assertThrows(AppException.class, () ->
                userMissionService.claimReward(user.getId(), mission.getId()));
        assertEquals(ErrorCode.INVALID_OPERATION, ex.getErrorCode());
    }

    @Test
    void incrementProgress_reachesTarget_setsCompleted() {
        when(missionProgressRepository.findByUserIdAndMissionId(user.getId(), mission.getId()))
                .thenReturn(Optional.of(progress));
        when(missionProgressRepository.save(any(MissionProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        MissionProgressResponse resp = userMissionService.incrementProgress(user.getId(), mission.getId(), 5);
        assertEquals(5, resp.getCurrentValue());
        assertEquals("COMPLETED", resp.getStatus());
    }
}
