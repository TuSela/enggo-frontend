package com.nhom12.enggo_backend.controller.social;

import com.nhom12.enggo_backend.dto.request.social.ChatMessagePayload;
import com.nhom12.enggo_backend.dto.response.social.MessageResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.social.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * Client gửi tin nhắn tới: /app/chat.send
     * Server broadcast tới tất cả thành viên trong conversation: /topic/conversation.{id}
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessagePayload payload, Principal principal) {
        // Lấy user từ JWT đã xác thực
        String username = principal.getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tái dùng ConversationService để lưu DB
        com.nhom12.enggo_backend.dto.request.social.SendMessageRequest req =
                new com.nhom12.enggo_backend.dto.request.social.SendMessageRequest(
                        payload.getContent(), payload.getType());

        MessageResponse saved = conversationService.sendMessage(sender.getId(), payload.getConversationId(), req);

        // Broadcast tới tất cả member đang subscribe conversation này
        messagingTemplate.convertAndSend(
                "/topic/conversation." + payload.getConversationId(),
                saved
        );

        log.info("Message sent to conversation {}: {}", payload.getConversationId(), payload.getContent());
    }
}