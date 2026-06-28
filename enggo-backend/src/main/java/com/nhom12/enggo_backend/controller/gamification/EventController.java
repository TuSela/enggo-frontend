package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.EventRequest;
import com.nhom12.enggo_backend.dto.response.gamification.EventResponse;
import com.nhom12.enggo_backend.service.gamification.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gamification/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    ApiResponse<List<EventResponse>> getEvents() {
        return ApiResponse.<List<EventResponse>>builder().result(eventService.getEvents()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<EventResponse> getEvent(@PathVariable Integer id) {
        return ApiResponse.<EventResponse>builder().result(eventService.getEvent(id)).build();
    }

    @PostMapping
    ApiResponse<EventResponse> createEvent(@RequestBody EventRequest request) {
        return ApiResponse.<EventResponse>builder().result(eventService.createEvent(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<EventResponse> updateEvent(@PathVariable Integer id, @RequestBody EventRequest request) {
        return ApiResponse.<EventResponse>builder().result(eventService.updateEvent(id, request)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
        return ApiResponse.<String>builder().result("Event has been deleted").build();
    }
}
