package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.MissionRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionResponse;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<MissionResponse> getMissions() {
        return missionRepository.findAll().stream().map(this::toMissionResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse getMission(Integer id) {
        return toMissionResponse(findMission(id));
    }

    public MissionResponse createMission(MissionRequest request) {
        Mission mission = new Mission();
        applyMissionRequest(mission, request);
        return toMissionResponse(missionRepository.save(mission));
    }

    public MissionResponse updateMission(Integer id, MissionRequest request) {
        Mission mission = findMission(id);
        applyMissionRequest(mission, request);
        return toMissionResponse(missionRepository.save(mission));
    }

    public void deleteMission(Integer id) {
        missionRepository.delete(findMission(id));
    }

    private void applyMissionRequest(Mission mission, MissionRequest request) {
        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setRewardExp(request.getRewardExp());
        mission.setMissionType(request.getMissionType());
        mission.setTargetValue(request.getTargetValue());
        mission.setMissionKey(request.getMissionKey());
        mission.setTimeLimitHours(request.getTimeLimitHours());
        mission.setStatus(request.getStatus());
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

    private Mission findMission(Integer id) {
        return missionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
