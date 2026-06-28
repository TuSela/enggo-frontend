package com.nhom12.enggo_backend.controller.gamification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.gamification.BadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.service.gamification.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/gamification/badges")
@RequiredArgsConstructor
public class BadgeController {
    private final BadgeService badgeService;
    private final ObjectMapper objectMapper;

    @GetMapping
    ApiResponse<List<BadgeResponse>> getBadges() {
        return ApiResponse.<List<BadgeResponse>>builder().result(badgeService.getBadges()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<BadgeResponse> getBadge(@PathVariable Integer id) {
        return ApiResponse.<BadgeResponse>builder().result(badgeService.getBadge(id)).build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BadgeResponse> createBadgeAdvanced(
            @RequestPart("request") String request, // Nhận cấu phần JSON
            MultipartHttpServletRequest servletRequest
    ) throws IOException {

        BadgeResponse response = badgeService.createBadge(toBadgeRequest(request), getBadgeFile(servletRequest));

        return ApiResponse.<BadgeResponse>builder()
                .result(response)
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<BadgeResponse> updateBadge(
            @PathVariable Integer id,
            @RequestPart("request") String request,
            MultipartHttpServletRequest servletRequest
    ) throws IOException {
        return ApiResponse.<BadgeResponse>builder()
                .result(badgeService.updateBadge(id, toBadgeRequest(request), getBadgeFile(servletRequest)))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteBadge(@PathVariable Integer id) {
        badgeService.deleteBadge(id);
        return ApiResponse.<String>builder().result("Badge has been deleted").build();
    }

    private BadgeRequest toBadgeRequest(String request) {
        try {
            return objectMapper.readValue(request, BadgeRequest.class);
        } catch (JsonProcessingException exception) {
            throw new AppException(ErrorCode.BADGE_REQUEST_INVALID);
        }
    }

    private MultipartFile getBadgeFile(MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        if (file == null) {
            file = request.getFile("badgeFile");
        }

        if (file == null) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        return file;
    }
}
