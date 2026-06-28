package com.nhom12.enggo_backend.configuration;

import com.nhom12.enggo_backend.configuration.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một broker đơn giản để gửi dữ liệu ngược về Client
        config.enableSimpleBroker("/topic", "/queue");
        // Các API Client gửi lên Server sẽ có tiền tố này (ví dụ: /app/find-match)
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user"); // Thêm: để gửi tin tới user cụ thể
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cổng thuần túy dành cho Postman / Android không dùng SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*").withSockJS();;

        // Cổng hỗ trợ SockJS dành cho file HTML/JS test trên trình duyệt
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint thuần WebSocket (không SockJS) — dành cho Android native / Postman
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Xác thực JWT mỗi khi client kết nối WebSocket
        registration.interceptors(webSocketAuthInterceptor);
    }
}