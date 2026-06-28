package com.nhom12.enggo_backend.controller.gamification;

import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.response.gamification.ExamPvpDisplayResponse;
import com.nhom12.enggo_backend.dto.response.gamification.MatchResultResponse;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.dto.response.gamification.QuizProgressResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.gamification.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class MatchController {
    @Autowired
    private UserRepository userRepository;

    private final MatchmakingService matchmakingService;
    private final SimpMessagingTemplate messagingTemplate;

    private final ConcurrentHashMap<Integer, Integer> readyPlayersCount = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public MatchController(MatchmakingService matchmakingService, SimpMessagingTemplate messagingTemplate) {
        this.matchmakingService = matchmakingService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/find-match")
    public void findMatch(Principal principal) {
        System.out.println("FIND MATCH đã được gọi!!!!!!!!!!!");
        User player2 = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Player 2 not found"));
        PvpMatchResponse matchResponse = matchmakingService.findMatch(player2);

        if (matchResponse != null) {
            int matchId = matchResponse.getId(); // Gi? s? PvpMatchResponse c� getId() c?a tr?n d?u
            readyPlayersCount.put(matchId, 0); // Kh?i t?o s? ngu?i s?n s�ng c?a tr?n n�y l� 0

            // 1. B?n th�ng tin t�m th?y tr?n v? cho 2 b�n hi?n th? giao di?n �?m ngu?c S?n s�ng
            messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer1Id(), matchResponse);
            messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer2Id(), matchResponse);

            // 2. ?? CO CH? TIMEOUT: H?n gi? sau 15 gi�y t? d?ng ki?m tra xem 2 b�n c� v�o d? kh�ng
            scheduler.schedule(() -> {
                Integer currentReadyCount = readyPlayersCount.get(matchId);

                if (currentReadyCount != null && currentReadyCount < 2) {
                    // C� �t nh?t 1 ngu?i kh�ng s?n s�ng! Ti?n h�nh h?y tr?n d?u
                    System.out.println("Tr?n d?u " + matchId + " b? h?y do c� ngu?i kh�ng s?n s�ng.");

                    // G?i Service x�a tr?n d?u n�y trong DB ho?c c?p nh?t status = "CANCELLED"
                    matchmakingService.cancelFindMatch(matchId);

                    // Th�ng b�o cho c? 2 m�y h?y m�n h�nh ch?, tr? v? s?nh ch�nh
                    messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer1Id(), "MATCH_TIMEOUT");
                    messagingTemplate.convertAndSend("/topic/match/" + matchResponse.getPlayer2Id(), "MATCH_TIMEOUT");

                    // X�a ph�ng kh?i b? nh? t?m
                    readyPlayersCount.remove(matchId);
                }
            }, 15, TimeUnit.SECONDS); // Ch? d�ng 15 gi�y
        } else {
            messagingTemplate.convertAndSendToUser(
                    player2.getUsername(), // hoặc principal.getName()
                    "/queue/queue-status",
                    "WAITING");
            System.out.println("User " + player2.getUsername() + " đang xếp hàng chờ (WAITING)...");
        }
    }
    @MessageMapping("/join-queue")
    public void joinQueue(Integer matchId, Principal principal) {
        System.out.println("Ten nguoi san sang: " + principal.getName());
        User player = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        readyPlayersCount.merge(matchId, 1, Integer::sum);

        if (readyPlayersCount.get(matchId) == 2) {

            ExamPvpDisplayResponse examDisplayResponse = matchmakingService.startPvpExam(matchId, player);
            examDisplayResponse.setMatchId(matchId);
            System.out.println("Id tran dau: " + matchId);
            if (examDisplayResponse != null) {
                // B?n d? thi v? cho c? tr?n
                messagingTemplate.convertAndSend("/topic/match/" + matchId, examDisplayResponse);
            }
            // X�a kh?i b? nh? t?m v� tr?n d?u d� ch�nh th?c b?t d?u l�m b�i
            readyPlayersCount.remove(matchId);
        } else {
            // Ngu?i n�y s?n s�ng r?i nhung v?n ph?i d?i ngu?i kia b?m
            messagingTemplate.convertAndSend("/topic/queue-status/" + player.getId(), "WAITING_FOR_ENEMY_READY");
        }
    }
    @MessageMapping("/leave-queue")
    public void leaveQueue(@Payload Integer userId) {
        matchmakingService.cancelFindMatch(userId);
        messagingTemplate.convertAndSend("/topic/queue-status/" + userId, "CANCELLED");
    }
    @MessageMapping("/match/{matchId}/progress")
    public void handleQuizProgress(
            @DestinationVariable Integer matchId,Principal principal,
            ExamAnswerRequest request) {
        QuizProgressResponse broadcastData = matchmakingService.playing(matchId, request, principal);
        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/progress", broadcastData);
    }
    @MessageMapping("/match/{matchId}/submit")
    public void handleQuizSubmit(@DestinationVariable Integer matchId, ExamSubmitRequest request, Principal principal) {
        MatchResultResponse result = matchmakingService.submitPvP(matchId,request,principal);
        if (result != null) {
            messagingTemplate.convertAndSend("/topic/match/" + matchId + "/result", result);
        }
    }
}
