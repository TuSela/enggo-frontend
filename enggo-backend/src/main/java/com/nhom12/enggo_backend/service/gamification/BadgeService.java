package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.BadgeRequest;
import com.nhom12.enggo_backend.dto.response.gamification.BadgeResponse;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.service.upload.UploadsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadgeService {
    private final BadgeRepository badgeRepository;
    @Autowired
    private UploadsService uploadsService;
    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges() {
        return badgeRepository.findAll().stream().map(this::toBadgeResponse).toList();
    }

    @Transactional(readOnly = true)
    public BadgeResponse getBadge(Integer id) {
        return toBadgeResponse(findBadge(id));
    }

    public BadgeResponse createBadge(BadgeRequest request, MultipartFile file) throws IOException {
        Badge badge = new Badge();
        applyBadgeRequest(badge, request, file);
        return toBadgeResponse(badgeRepository.save(badge));
    }

    public BadgeResponse updateBadge(Integer id, BadgeRequest request, MultipartFile file) throws IOException {
        Badge badge = findBadge(id);
        applyBadgeRequest(badge, request, file);
        return toBadgeResponse(badgeRepository.save(badge));
    }

    public void deleteBadge(Integer id) {
        badgeRepository.delete(findBadge(id));
    }

    private void applyBadgeRequest(Badge badge, BadgeRequest request, MultipartFile file) throws IOException {
        validateBadgeRequest(request);

        badge.setBadgeName(request.getBadgeName());
        badge.setDescription(request.getDescription());
//        xử lý ảnh
        String imageUrl = uploadsService.uploadImage(file);
        badge.setIconUrl(imageUrl);

        badge.setCreatedAt(LocalDateTime.now());
    }

    private void validateBadgeRequest(BadgeRequest request) {
        if (request.getBadgeName() == null || request.getBadgeName().isBlank()) {
            throw new AppException(ErrorCode.BADGE_NAME_REQUIRED);
        }

        if (request.getBadgeName().length() > 100) {
            throw new AppException(ErrorCode.BADGE_NAME_INVALID);
        }
    }

    private BadgeResponse toBadgeResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .badgeName(badge.getBadgeName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .createdAt(badge.getCreatedAt())
                .build();
    }

    private Badge findBadge(Integer id) {
        return badgeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
