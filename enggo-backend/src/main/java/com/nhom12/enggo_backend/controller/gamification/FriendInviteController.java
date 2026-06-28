package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import com.nhom12.enggo_backend.dto.request.gamification.InviteRequest;
import com.nhom12.enggo_backend.dto.response.gamification.InviteResponse;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.exam.ExamGenerationService;
import com.nhom12.enggo_backend.service.gamification.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class FriendInviteController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final MatchmakingService matchmakingService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Luu c�c loi moi dang cho: inviteId -> InviteResponse
    private final ConcurrentHashMap<Integer, InviteResponse> pendingInvites = new ConcurrentHashMap<>();
    private final AtomicInteger inviteIdCounter = new AtomicInteger(0);

    public FriendInviteController(SimpMessagingTemplate messagingTemplate,
                                  UserRepository userRepository,
                                  MatchmakingService matchmakingService) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.matchmakingService = matchmakingService;
    }

    // 1 Player A gui loi moi
    @MessageMapping("/invite/send")
    public void sendInvite(InviteRequest request, Principal principal) {
        // Helper to send updated pending invite count to a user
        // (will be defined later in this class)
        User inviter = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        User invitee = userRepository.findByUsername(request.getInviteeUsername())
                .orElseThrow(() -> new RuntimeException("Invitee not found"));

        int inviteId = inviteIdCounter.incrementAndGet();

        InviteResponse invite = InviteResponse.builder()
                .inviteId(inviteId)
                .inviterPlayerId(inviter.getId())
                .inviteePlayerId(invitee.getId())
                .inviterUsername(inviter.getUsername())
                .inviteeUsername(invitee.getUsername())
                .randomBlueprintRequest(request.getRandomBlueprintRequest())
                .status("PENDING")
                .build();

        pendingInvites.put(inviteId, invite);

        // Gui loi moi den B
        messagingTemplate.convertAndSend(
                "/topic/invite/" + invitee.getId(), invite
        );

        // Timeout 30 gi�y neu B kh�ng phan hoi
        scheduler.schedule(() -> {
            InviteResponse current = pendingInvites.get(inviteId);

            if (current != null && "PENDING".equals(current.getStatus())) {
                pendingInvites.remove(inviteId);

                // Th�ng b�o A rang B kh�ng phan hoi
                messagingTemplate.convertAndSend(
                        "/topic/invite-result/" + inviter.getId(), "INVITE_TIMEOUT"
                );
                // Th�ng b�o B rang loi moi da het han
                messagingTemplate.convertAndSend(
                        "/topic/invite-result/" + invitee.getId(), "INVITE_EXPIRED"
                );
            }
        }, 30, TimeUnit.SECONDS);
    }

    // 2 Player B phan hoi loi moi
    @MessageMapping("/invite/respond")
    public void respondInvite(@Payload Map<String, Object> payload, Principal principal) {
        Object inviteIdObj = payload.get("inviteId");
        Integer inviteId = null;
        if (inviteIdObj instanceof Integer) {
            inviteId = (Integer) inviteIdObj;
        } else if (inviteIdObj instanceof Number) {
            inviteId = ((Number) inviteIdObj).intValue();
        }

        Boolean accepted = (Boolean) payload.get("accepted");

        if (inviteId == null) return;

        InviteResponse invite = pendingInvites.get(inviteId);

        // Loi moi kh�ng ton tai hoac da het han
        if (invite == null) {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            messagingTemplate.convertAndSend(
                    "/topic/invite-result/" + user.getId(), "INVITE_EXPIRED"
            );
            return;
        }

        if (!Boolean.TRUE.equals(accepted)) {
            // B tu choi
            invite.setStatus("DECLINED");
            pendingInvites.remove(inviteId);

            messagingTemplate.convertAndSend(
                    "/topic/invite-result/" + invite.getInviterPlayerId(), "INVITE_DECLINED"
            );
            return;
        }

        // B dong � -> tao tran dau
        invite.setStatus("ACCEPTED");
        pendingInvites.remove(inviteId);

        // T�i dung matchmakingService de tao tran tu 2 player cu the
        PvpMatchResponse matchResponse = matchmakingService.createDirectMatch(
                invite.getInviterPlayerId(),
                invite.getInviteePlayerId(),
                invite.getRandomBlueprintRequest()
        );
        if (matchResponse != null) {
            // Gui th�ng tin tran dau cho ca 2
            messagingTemplate.convertAndSend(
                    "/topic/match/" + invite.getInviterPlayerId(), matchResponse
            );
            messagingTemplate.convertAndSend(
                    "/topic/match/" + invite.getInviteePlayerId(), matchResponse
            );
        }
    }
}
