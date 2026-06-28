package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.EventRequest;
import com.nhom12.enggo_backend.dto.response.gamification.EventResponse;
import com.nhom12.enggo_backend.entity.gamification.Event;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.gamification.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents() {
        return eventRepository.findAll().stream().map(this::toEventResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer id) {
        return toEventResponse(findEvent(id));
    }

    public EventResponse createEvent(EventRequest request) {
        Event event = new Event();
        applyEventRequest(event, request);
        return toEventResponse(eventRepository.save(event));
    }

    public EventResponse updateEvent(Integer id, EventRequest request) {
        Event event = findEvent(id);
        applyEventRequest(event, request);
        return toEventResponse(eventRepository.save(event));
    }

    public void deleteEvent(Integer id) {
        eventRepository.delete(findEvent(id));
    }

    private void applyEventRequest(Event event, EventRequest request) {
        event.setEventName(request.getEventName());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRequiredLevel(request.getRequiredLevel());
        event.setStatus(request.getStatus());
        event.setEventType(request.getEventType());
        event.setBannerUrl(request.getBannerUrl());
    }

    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .requiredLevel(event.getRequiredLevel())
                .status(event.getStatus())
                .eventType(event.getEventType())
                .bannerUrl(event.getBannerUrl())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private Event findEvent(Integer id) {
        return eventRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
