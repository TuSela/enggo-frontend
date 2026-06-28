package com.nhom12.enggo_backend.configuration;

import com.nhom12.enggo_backend.repository.InvalidatedTokenRepository;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        log.info("STOMP Command: {}", accessor.getCommand());

        // Chỉ xác thực khi CONNECT
        if (!StompCommand.CONNECT.equals(accessor.getCommand())) return message;

        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        log.info("Authorization headers: {}", authHeaders);

        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("❌ Thiếu header Authorization");
            // KHÔNG throw exception — trả về message để tránh disconnect đột ngột
            return message;
        }

        String token = authHeaders.get(0).replace("Bearer ", "").trim();

        try {
            var verifier = new MACVerifier(SIGNER_KEY.getBytes());
            var signedJWT = SignedJWT.parse(token);

            boolean verified = signedJWT.verify(verifier);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean notExpired = expiryTime != null && expiryTime.after(new Date());
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            boolean invalidated = invalidatedTokenRepository.existsById(jwtId);

            if (!verified || !notExpired || invalidated) {
                log.warn("❌ Token không hợp lệ: verified={}, notExpired={}, invalidated={}",
                        verified, notExpired, invalidated);
                return message; // KHÔNG throw, tránh disconnect
            }

            String username = signedJWT.getJWTClaimsSet().getSubject();
            log.info("✅ WebSocket authenticated: {}", username);

            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
            accessor.setUser(auth);

        } catch (Exception e) {
            log.error("❌ JWT parse error: {}", e.getMessage());
            // KHÔNG throw exception
        }

        return message;
    }
}