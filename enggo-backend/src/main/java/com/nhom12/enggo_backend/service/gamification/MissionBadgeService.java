package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.MissionBadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.MissionBadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionBadge;
import com.nhom12.enggo_backend.entity.gamification.MissionBadgeId;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionBadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionBadgeService {
    private final MissionBadgeRepository missionBadgeRepository;
    private final MissionRepository missionRepository;
    private final BadgeRepository badgeRepository;

    @Transactional(readOnly = true)
    public List<MissionBadgeResponse> getMissionBadges() {
        return missionBadgeRepository.findAll().stream().map(this::toMissionBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public MissionBadgeResponse getMissionBadge(Integer missionId, Integer badgeId) {
        return toMissionBadgeResponse(findMissionBadge(missionId, badgeId));
    }

    public MissionBadgeResponse createMissionBadge(MissionBadgeRequest request) {
        MissionBadgeId id = new MissionBadgeId(request.getMissionId(), request.getBadgeId());
        if (missionBadgeRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        MissionBadge missionBadge = MissionBadge.builder()
                .id(id)
                .mission(findMission(request.getMissionId()))
                .badge(findBadge(request.getBadgeId()))
                .bonusExp(request.getBonusExp())
                .build();
        return toMissionBadgeResponse(missionBadgeRepository.save(missionBadge));
    }

    public MissionBadgeResponse updateMissionBadge(Integer missionId, Integer badgeId, MissionBadgeRequest request) {
        MissionBadge missionBadge = findMissionBadge(missionId, badgeId);
        if (!missionId.equals(request.getMissionId()) || !badgeId.equals(request.getBadgeId())) {
            missionBadgeRepository.delete(missionBadge);
            return createMissionBadge(request);
        }
        missionBadge.setBonusExp(request.getBonusExp());
        return toMissionBadgeResponse(missionBadgeRepository.save(missionBadge));
    }

    public void deleteMissionBadge(Integer missionId, Integer badgeId) {
        missionBadgeRepository.delete(findMissionBadge(missionId, badgeId));
    }

    private MissionBadgeResponse toMissionBadgeResponse(MissionBadge missionBadge) {
        return MissionBadgeResponse.builder()
                .missionId(missionBadge.getMission().getId())
                .missionTitle(missionBadge.getMission().getTitle())
                .badgeId(missionBadge.getBadge().getId())
                .badgeName(missionBadge.getBadge().getBadgeName())
                .bonusExp(missionBadge.getBonusExp())
                .build();
    }

    private MissionBadge findMissionBadge(Integer missionId, Integer badgeId) {
        return missionBadgeRepository.findById(new MissionBadgeId(missionId, badgeId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Mission findMission(Integer id) {
        return missionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Badge findBadge(Integer id) {
        return badgeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
