package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.BadgeEventRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeEventResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.gamification.BadgeEvent;
import com.nhom12.enggo_backend.entity.gamification.BadgeEventId;
import com.nhom12.enggo_backend.entity.gamification.Event;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.gamification.BadgeEventRepository;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.repository.gamification.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeEventService {
    private final BadgeEventRepository badgeEventRepository;
    private final BadgeRepository badgeRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<BadgeEventResponse> getBadgeEvents() {
        return badgeEventRepository.findAll().stream().map(this::toBadgeEventResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeEventResponse getBadgeEvent(Integer badgeId, Integer eventId) {
        return toBadgeEventResponse(findBadgeEvent(badgeId, eventId));
    }

    public BadgeEventResponse createBadgeEvent(BadgeEventRequest request) {
        BadgeEventId id = new BadgeEventId(request.getBadgeId(), request.getEventId());
        if (badgeEventRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_EXISTED);
        }
        BadgeEvent badgeEvent = BadgeEvent.builder()
                .id(id)
                .badge(findBadge(request.getBadgeId()))
                .event(findEvent(request.getEventId()))
                .minScoreRequired(request.getMinScoreRequired())
                .rewardExp(request.getRewardExp())
                .build();
        return toBadgeEventResponse(badgeEventRepository.save(badgeEvent));
    }

    public BadgeEventResponse updateBadgeEvent(Integer badgeId, Integer eventId, BadgeEventRequest request) {
        BadgeEvent badgeEvent = findBadgeEvent(badgeId, eventId);
        if (!badgeId.equals(request.getBadgeId()) || !eventId.equals(request.getEventId())) {
            badgeEventRepository.delete(badgeEvent);
            return createBadgeEvent(request);
        }
        badgeEvent.setMinScoreRequired(request.getMinScoreRequired());
        badgeEvent.setRewardExp(request.getRewardExp());
        return toBadgeEventResponse(badgeEventRepository.save(badgeEvent));
    }

    public void deleteBadgeEvent(Integer badgeId, Integer eventId) {
        badgeEventRepository.delete(findBadgeEvent(badgeId, eventId));
    }

    private BadgeEventResponse toBadgeEventResponse(BadgeEvent badgeEvent) {
        return BadgeEventResponse.builder()
                .badgeId(badgeEvent.getBadge().getId())
                .badgeName(badgeEvent.getBadge().getBadgeName())
                .eventId(badgeEvent.getEvent().getId())
                .eventName(badgeEvent.getEvent().getEventName())
                .minScoreRequired(badgeEvent.getMinScoreRequired())
                .rewardExp(badgeEvent.getRewardExp())
                .build();
    }

    private BadgeEvent findBadgeEvent(Integer badgeId, Integer eventId) {
        return badgeEventRepository.findById(new BadgeEventId(badgeId, eventId))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Badge findBadge(Integer id) {
        return badgeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Event findEvent(Integer id) {
        return eventRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
