package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.ApiResponse;
import com.nhom12.enggo_backend.dto.request.social.SendMessageRequest;
import com.nhom12.enggo_backend.dto.response.social.ConversationResponse;
import com.nhom12.enggo_backend.dto.response.social.MessageResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.social.ConversationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/social/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;
    UserRepository userRepository;

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            return user.getId();
        }
        throw new RuntimeException("Chưa xác thực");
    }

    // 1. Mở / tạo cuộc trò chuyện với bạn bè
    @PostMapping("/private/{targetUserId}")
    public ApiResponse<ConversationResponse> openPrivateChat(@PathVariable Integer targetUserId) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.getOrCreatePrivateConversation(getCurrentUserId(), targetUserId))
                .build();
    }

    // 2. Lấy danh sách hội thoại (inbox)
    @GetMapping
    public ApiResponse<List<ConversationResponse>> getMyConversations() {
        return ApiResponse.<List<ConversationResponse>>builder()
                .result(conversationService.getMyConversations(getCurrentUserId()))
                .build();
    }

    // 3. Gửi tin nhắn
    @PostMapping("/{conversationId}/messages")
    public ApiResponse<MessageResponse> sendMessage(
            @PathVariable Integer conversationId,
            @RequestBody SendMessageRequest request) {
        return ApiResponse.<MessageResponse>builder()
                .result(conversationService.sendMessage(getCurrentUserId(), conversationId, request))
                .build();
    }

    // 4. Lấy lịch sử tin nhắn (phân trang)
    @GetMapping("/{conversationId}/messages")
    public ApiResponse<Page<MessageResponse>> getMessages(
            @PathVariable Integer conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<MessageResponse>>builder()
                .result(conversationService.getMessages(getCurrentUserId(), conversationId, page, size))
                .build();
    }
}