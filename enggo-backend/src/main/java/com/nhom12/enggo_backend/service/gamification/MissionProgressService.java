package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.MissionProgressRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionProgressResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionProgressService {
    private static final int MISSIONS_PER_LOGIN = 3;
    private final MissionProgressRepository missionProgressRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<MissionProgressResponse> getMissionProgresses() {
        return missionProgressRepository.findAll().stream().map(this::toMissionProgressResponse).toList();
    }
    @Transactional(readOnly = true)
    public MissionProgressResponse getMissionProgress(Integer id) {
        return toMissionProgressResponse(findMissionProgress(id));
    }
    public MissionProgressResponse createMissionProgress(MissionProgressRequest request) {
        MissionProgress progress = new MissionProgress();
        applyMissionProgressRequest(progress, request);
        return toMissionProgressResponse(missionProgressRepository.save(progress));
    }
    public MissionProgressResponse updateMissionProgress(Integer id, MissionProgressRequest request) {
        MissionProgress progress = findMissionProgress(id);
        applyMissionProgressRequest(progress, request);
        return toMissionProgressResponse(missionProgressRepository.save(progress));
    }
    public void deleteMissionProgress(Integer id) {
        missionProgressRepository.delete(findMissionProgress(id));
    }
    private void applyMissionProgressRequest(MissionProgress progress, MissionProgressRequest request) {
        progress.setUser(findUser(request.getUserId()));
        progress.setMission(findMission(request.getMissionId()));
        progress.setCurrentValue(request.getCurrentValue());
        progress.setStatus(request.getStatus());
        progress.setDeadline(request.getDeadline());
    }
    private MissionProgressResponse toMissionProgressResponse(MissionProgress progress) {
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

    private MissionProgress findMissionProgress(Integer id) {
        return missionProgressRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Mission findMission(Integer id) {
        return missionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
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
}
