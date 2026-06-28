package com.nhom12.enggo_backend.configuration;

import com.nhom12.enggo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor

public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Tự động bắn khi client kết nối WebSocket thành công
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : null;
        if (username == null) return;

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus("ONLINE");
            userRepository.save(user);
            log.info("{} is ONLINE", username);

            // Thông báo tới tất cả client đang lắng nghe /topic/status
            messagingTemplate.convertAndSend("/topic/status", new StatusPayload(user.getId(), "ONLINE"));
        });
    }

    // Tự động bắn khi client ngắt kết nối (đóng app, mất mạng, logout)
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : null;
        if (username == null) return;

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus("OFFLINE");
            userRepository.save(user);
            log.info("{} is OFFLINE", username);

            // Thông báo tới tất cả client
            messagingTemplate.convertAndSend("/topic/status", new StatusPayload(user.getId(), "OFFLINE"));
        });
    }

    // DTO nhỏ gọn gửi qua WebSocket
    public record StatusPayload(Integer userId, String status) {}
}