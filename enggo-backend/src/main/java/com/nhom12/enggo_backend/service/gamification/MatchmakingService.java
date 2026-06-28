package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.request.exam.RandomBlueprintRequest;
import com.nhom12.enggo_backend.dto.response.gamification.*;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.exam.ExamAttemptDetail;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.gamification.PvpMatch;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.mapper.exam.ExamMapper;
import com.nhom12.enggo_backend.mapper.gamificationMapper.BadgeMapper;
import com.nhom12.enggo_backend.repository.exam.ExamAttemptRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.exam.QuestionRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.PvpMatchRepository;
import com.nhom12.enggo_backend.repository.gamification.BadgeRepository;
import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.dto.request.gamification.UserBadgeRequest;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.service.UserService;
import com.nhom12.enggo_backend.service.exam.ExamAttemptService;
import com.nhom12.enggo_backend.service.exam.ScoreCheck;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeService userBadgeService;
    private final UserMissionService userMissionService;
    private final MissionProgressRepository progressRepository;
    private static final Logger log = LoggerFactory.getLogger(MatchmakingService.class);

    public void checkMissionExam(PvpMatch pvpMatch) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();          // 00:00
        LocalDateTime endOfDay   = today.atTime(LocalTime.MAX); // 23:59:59.999...

        // 3️⃣ Các missionType cần kiểm tra
        List<String> targetTypes = List.of("PVP","PVP-FRIEND","PVP_PERFECT_SCORE","PVP_SPEED","PVP_STREAK");

        // 4️⃣ Lấy tất cả progress của hôm nay và lọc theo missionType
        List<MissionProgress> todayProgress = progressRepository
                .findByUserIdAndDeadlineBetween(pvpMatch.getPlayer1().getId(), startOfDay, endOfDay)
                .stream()
                .filter(p -> p.getMission() != null &&
                        targetTypes.contains(p.getMission().getMissionType()))
                // Bỏ qua mission đã CLAIMED để tránh AppException khiến transaction
                // finalizeMatch bị Spring đánh dấu rollback-only.
                .filter(p -> !"CLAIMED".equals(p.getStatus()))
                .collect(Collectors.toList());

        // Logger variable removed – will use class‑level logger
        if (todayProgress.isEmpty()) {
            log.debug("User {} has no QUIZ‑related missions for today.", pvpMatch.getPlayer1().getUsername());
            return;
        }

        // 5️⃣ Giá trị tăng (ở đây +1, có thể thay bằng attempt.getScore() …)
        int increment = 1;

        // 6️⃣ Cập nhật tiến độ qua service đã có (chạy trong transaction riêng,
        // không thể làm rollback transaction finalizeMatch đang chứa exp/elo/streak)
        for (MissionProgress mp : todayProgress) {
            userMissionService.incrementProgressSafely(pvpMatch.getPlayer1().getId(),
                    mp.getMission().getId(),
                    increment);
            log.debug("Incremented mission {} (type={}) for user {} by {}.",
                    mp.getMission().getId(),
                    mp.getMission().getMissionType(),
                    pvpMatch.getPlayer1().getUsername(),
                    increment);
        }
        List<MissionProgress> todayProgress2 = progressRepository
                .findByUserIdAndDeadlineBetween(pvpMatch.getPlayer2().getId(), startOfDay, endOfDay)
                .stream()
                .filter(p -> p.getMission() != null &&
                        targetTypes.contains(p.getMission().getMissionType()))
                .filter(p -> !"CLAIMED".equals(p.getStatus()))
                .collect(Collectors.toList());

        // Logger variable removed – will use class‑level logger
        if (todayProgress2.isEmpty()) {
            log.debug("User {} has no QUIZ‑related missions for today.", pvpMatch.getPlayer2().getUsername());
            return;
        }

        // 5️⃣ Giá trị tăng (ở đây +1, có thể thay bằng attempt.getScore() …)
        int increment2 = 1;

        // 6️⃣ Cập nhật tiến độ qua service đã có
        for (MissionProgress mp : todayProgress2) {
            userMissionService.incrementProgressSafely(pvpMatch.getPlayer2().getId(),
                    mp.getMission().getId(),
                    increment2);
            log.debug("Incremented mission {} (type={}) for user {} by {}.",
                    mp.getMission().getId(),
                    mp.getMission().getMissionType(),
                    pvpMatch.getPlayer2().getUsername(),
                    increment2);
        }
    }


    private final StringRedisTemplate redisTemplate;
    private final PvpMatchRepository pvpMatchRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamGenerationPVPService examGenerationPVPService;
    private final ExamAttemptService examAttemptService;
    private final QuestionRepository questionRepository;
    private final BadgeMapper badgeMapper;
    private final StreakService streakService;

    @Autowired
    private UserService userService;

    private static final String MATCH_QUEUE_KEY = "pvp:match:queue";

    @Transactional
    public synchronized PvpMatchResponse findMatch(User player2) {
        String userIdStr = String.valueOf(player2.getId());
        Boolean isWaiting = redisTemplate.opsForSet().isMember(MATCH_QUEUE_KEY, userIdStr);
        if (Boolean.TRUE.equals(isWaiting)) {
            return null;
        }

        String opponentIdStr = redisTemplate.opsForSet().pop(MATCH_QUEUE_KEY);

        if (opponentIdStr == null) {
            redisTemplate.opsForSet().add(MATCH_QUEUE_KEY, userIdStr);
            return null;
        }

        Integer opponentId = Integer.parseInt(opponentIdStr);

        User player1 = userRepository.findById(opponentId)
                .orElseThrow(() -> new RuntimeException("Player 1 not found"));

        Exam randomExam = examRepository.findRandomExam()
                .orElseThrow(() -> new RuntimeException("No exam available for PVP match"));

        PvpMatch newMatch = PvpMatch.builder()
                .player1(player1)
                .player2(player2)
                .exam(randomExam)
                .status(String.valueOf(MatchStatus.PLAYING))
                .player1Score(0)
                .player2Score(0)
                .startTime(LocalDateTime.now())
                .build();

        PvpMatch savedMatch = pvpMatchRepository.save(newMatch);

        return PvpMatchResponse.builder()
                .id(savedMatch.getId())
                .player1Id(player1.getId())
                .avatarUrlP1(player1.getAvatarUrl())
                .player1Username(player1.getUsername())
                .eloP1(player1.getElo())
                .player2Id(player2.getId())
                .avatarUrlP2(player2.getAvatarUrl())
                .player2Username(player2.getUsername())
                .eloP2(player2.getElo())
                .examId(savedMatch.getExam().getId())
                .examTitle(savedMatch.getExam().getTitle())
                .status(savedMatch.getStatus())
                .startTime(savedMatch.getStartTime())
                .build();
    }

    @Transactional
    public PvpMatchResponse createDirectMatch(Integer player1Id, Integer player2Id, RandomBlueprintRequest request) {
        System.out.println("Themes: " + request.getThemeIds() +"so luong: "+ request.getTotalQuestions() + "Do kho: "+ request.getDifficulty()+"loai cau hoi: "+ request.getQuestionTypes());

        User player1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new RuntimeException("Player 1 not found"));
        User player2 = userRepository.findById(player2Id)
                .orElseThrow(() -> new RuntimeException("Player 2 not found"));


        Exam randomExam = examGenerationPVPService.getOrGenerateExamResponse(request,player1);

        PvpMatch newMatch = PvpMatch.builder()
                .player1(player1)
                .player2(player2)
                .exam(randomExam)
                .status(String.valueOf(MatchStatus.PLAYING))
                .player1Score(0)
                .player2Score(0)
                .startTime(LocalDateTime.now())
                .hostUserId(player1Id)
                .build();
        PvpMatch savedMatch = pvpMatchRepository.save(newMatch);

        return PvpMatchResponse.builder()
                .id(savedMatch.getId())
                .player1Id(player1.getId())
                .avatarUrlP1(player1.getAvatarUrl())
                .player1Username(player1.getUsername())
                .eloP1(player1.getElo())
                .player2Id(player2.getId())
                .avatarUrlP2(player2.getAvatarUrl())
                .player2Username(player2.getUsername())
                .eloP2(player2.getElo())
                .examId(savedMatch.getExam().getId())
                .examTitle(savedMatch.getExam().getTitle())
                .status(savedMatch.getStatus())
                .startTime(savedMatch.getStartTime())
                .hostUserId(player1Id)
                .difficulty(savedMatch.getExam().getDifficulty())
                .totalQuestions(savedMatch.getExam().getTotalQuestions())
                .themeIds(new ArrayList<Integer>())
                .build();
    }

    public void cancelFindMatch(Integer userId) {
        redisTemplate.opsForSet().remove(MATCH_QUEUE_KEY, String.valueOf(userId));
    }


    @Transactional
    public QuizProgressResponse playing(Integer matchId, ExamAnswerRequest request, Principal principal){
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        System.out.println("Id tran dau: " + matchId);
        PvpMatch pvpMatch = pvpMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match Not Found"));

        var question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question Not Found"));

        // Ki?m tra cu h?i c trong d? thi c?a tr?n d?u khng
        boolean isBelong = pvpMatch.getExam().getExamQuestions().stream()
                .anyMatch(e -> e.getQuestion().getId().equals(question.getId()));
        if (!isBelong) {
            throw new RuntimeException("Question Not Found in Exam");
        }

        ScoreCheck result = examAttemptService.scoreCheck(question, request,pvpMatch.getExam());

        int currentScore = 0;
        if (pvpMatch.getPlayer1().getId().equals(user.getId())) {
            if (result.isCorrect()) {
                pvpMatch.setPlayer1Score((pvpMatch.getPlayer1Score() == null ? 0 : pvpMatch.getPlayer1Score()) + 10);
            }
            currentScore = pvpMatch.getPlayer1Score();
        } else if (pvpMatch.getPlayer2().getId().equals(user.getId())) {
            if (result.isCorrect()) {
                pvpMatch.setPlayer2Score((pvpMatch.getPlayer2Score() == null ? 0 : pvpMatch.getPlayer2Score()) + 10);
            }
            currentScore = pvpMatch.getPlayer2Score();
        }

        pvpMatchRepository.save(pvpMatch);

        return QuizProgressResponse.builder()
                .userId(user.getId())
                .currentScore(currentScore)
                .isCorrect(result.isCorrect())
                .questionId(request.getQuestionId())
                .build();
    }
    @Transactional
    // ?? Hm API dnh ring cho mn hnh PvP QuizActivity l?y d?
    public ExamPvpDisplayResponse startPvpExam (Integer matchId, User playerId) {
        // Log start of method for debugging
        System.out.println("[DEBUG] startPvpExam called with matchId=" + matchId);
        // Retrieve the match and both participants
        PvpMatch pvpMatch = pvpMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("L?I: Khng tm th?y PVPMatch : "));
        User player2 = pvpMatch.getPlayer2();
        User player1 = pvpMatch.getPlayer1();
        var exam = pvpMatch.getExam();

        if (!exam.getActive()) {
            throw new IllegalStateException("Exam has been stopped");
        }

        boolean hasOngoing = examAttemptRepository.existsByUserIdAndCompleteFalse(player2.getId());
        boolean hasOngoing2 = examAttemptRepository.existsByUserIdAndCompleteFalse(player1.getId());


        if (hasOngoing && hasOngoing2) {
            throw new IllegalStateException("You have an ongoing attempt");
        }
        ExamAttempt attempt = ExamAttempt.builder()
                .user(player1)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt);

        ExamAttempt attempt2 = ExamAttempt.builder()
                .user(player2)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt2);

        // Link attempts to the match
        pvpMatch.setPlayer1Attempt(attempt);
        pvpMatch.setPlayer2Attempt(attempt2);
        pvpMatchRepository.save(pvpMatch);

        // Log detailed info for debugging
        System.out.println("[DEBUG] startPvpExam matchId=" + matchId
                + " examId=" + exam.getId()
                + " p1AttemptId=" + attempt.getId()
                + " p2AttemptId=" + attempt2.getId());

        return examMapper.toExamPvpDisplayResponse(exam, attempt, attempt2);
    }
    @Transactional
    public MatchResultResponse submitPvP(Integer matchId, ExamSubmitRequest request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new RuntimeException("User Not Found"));
        PvpMatch pvpMatch = pvpMatchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("Match Not Found"));
        if (pvpMatch.getPlayer1().getId().equals(user.getId())) {

            int correctCount = 0;
            BigDecimal pointsPerQuestion = BigDecimal.TEN.divide(BigDecimal.valueOf(pvpMatch.getExam().getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalScore = BigDecimal.ZERO;
            List<ExamAttemptDetail> details = new ArrayList<>();
            for (ExamAnswerRequest answer : request.getExamAnswers()) {
                var question = questionRepository.findById(answer.getQuestionId()).orElseThrow(() -> new RuntimeException("Question Not Found"));

                ScoreCheck result = examAttemptService.scoreCheck(question, answer,pvpMatch.getExam());
                if (result.isCorrect()) {
                    correctCount++;
                    totalScore = totalScore.add(pointsPerQuestion);
                }

                details.add(examAttemptService.saveDetail(pvpMatch.getPlayer1Attempt(), question, answer, result));
            }
            Duration duration = Duration.between(pvpMatch.getStartTime(), LocalDateTime.now());
            String timeSpent = duration.getSeconds() + "";
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
            pvpMatch.getPlayer1Attempt().setCorrectAnswersCount(correctCount);
            pvpMatch.getPlayer1Attempt().setCompletedAt(LocalDateTime.now());
            pvpMatch.getPlayer1Attempt().setTotalScore(totalScore);
            pvpMatch.getPlayer1Attempt().setComplete(true);
            pvpMatch.getPlayer1Attempt().setTimeSpent(timeSpent);
            examAttemptRepository.save(pvpMatch.getPlayer1Attempt());
        } else if (pvpMatch.getPlayer2().getId().equals(user.getId())) {
            int correctCount = 0;
            BigDecimal pointsPerQuestion = BigDecimal.TEN.divide(BigDecimal.valueOf(pvpMatch.getExam().getTotalQuestions()), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalScore = BigDecimal.ZERO;
            List<ExamAttemptDetail> details = new ArrayList<>();
            for (ExamAnswerRequest answer : request.getExamAnswers()) {
                var question = questionRepository.findById(answer.getQuestionId()).orElseThrow(() -> new RuntimeException("Question Not Found"));

                ScoreCheck result = examAttemptService.scoreCheck(question, answer,pvpMatch.getExam());
                if (result.isCorrect()) {
                    correctCount++;
                    totalScore = totalScore.add(pointsPerQuestion);
                }

                details.add(examAttemptService.saveDetail(pvpMatch.getPlayer2Attempt(), question, answer, result));
            }
            Duration duration = Duration.between(pvpMatch.getStartTime(), LocalDateTime.now());
            String timeSpent = duration.getSeconds() + "";
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
            pvpMatch.getPlayer2Attempt().setCorrectAnswersCount(correctCount);
            pvpMatch.getPlayer2Attempt().setCompletedAt(LocalDateTime.now());
            pvpMatch.getPlayer2Attempt().setTotalScore(totalScore);
            pvpMatch.getPlayer2Attempt().setComplete(true);
            pvpMatch.getPlayer2Attempt().setTimeSpent(timeSpent);
            examAttemptRepository.save(pvpMatch.getPlayer2Attempt());
        }
        pvpMatchRepository.save(pvpMatch);
        if (pvpMatch.getPlayer1Attempt().getComplete() && pvpMatch.getPlayer2Attempt().getComplete()) {
            return finalizeMatch(pvpMatch);
        }
        return null;
    }
    private MatchResultResponse finalizeMatch(PvpMatch match) {
        match.setStatus(String.valueOf(MatchStatus.FINISHED));
        match.setEndTime(LocalDateTime.now());
        int score1 = match.getPlayer1Score();
        int score2 = match.getPlayer2Score();
        Integer winnerId = null;
        User player1 = match.getPlayer1();
        User player2 = match.getPlayer2();

        int oldElo1 = player1.getElo() != null ? player1.getElo() : 75;
        int oldElo2 = player2.getElo() != null ? player2.getElo() : 75;

        int eloChange = 25;
        if (score1 > score2) {
            winnerId = player1.getId();
            match.setWinner(player1);
            updateElo(player1, player2, eloChange);
            player1.setPvpWins((player1.getPvpWins() == null ? 0 : player1.getPvpWins()) + 1);
            player1.incrementWinStreak();
            player2.resetWinStreak();
        } else if (score2 > score1) {
            winnerId = player2.getId();
            match.setWinner(player2);
            updateElo(player2, player1, eloChange);
            player2.setPvpWins((player2.getPvpWins() == null ? 0 : player2.getPvpWins()) + 1);
            player1.resetWinStreak();
            player2.incrementWinStreak();
        } else {
            updateElo(player1, player2, 0);
        }

        player1.setBadgeRank(awardBadgeIfEligible(player1));
        player2.setBadgeRank(awardBadgeIfEligible(player2));
        streakService.recordDailyActivity(player1);
        streakService.recordDailyActivity(player2);
        userRepository.save(player1);
        userRepository.save(player2);
        // Award badge based on updated elo
        BadgeResponse badgeResponse =badgeMapper.toBadgeResponse(player1.getBadgeRank());

        pvpMatchRepository.save(match);

        var p1Result = MatchResultResponse.PlayerResult.builder()
                .userName(player1.getUsername())
                .level(player1.getLevel())
                .avatarUrl(player1.getAvatarUrl())
                .playerScore(score1)
                .eloChange(player1.getElo() - oldElo1)
                .correctAnswersCount(match.getPlayer1Attempt().getCorrectAnswersCount())
                .elo(player1.getElo())
                .duration(match.getPlayer1Attempt().getTimeSpent())
                .totalQuestions(match.getExam().getTotalQuestions())
                .WinStreak(player1.getWinStreak())
                .badgeRank(badgeResponse)
                .build();
        BadgeResponse badgeResponse2 =badgeMapper.toBadgeResponse(player2.getBadgeRank());
        var p2Result = MatchResultResponse.PlayerResult.builder()
                .userName(player2.getUsername())
                .level(player2.getLevel())
                .avatarUrl(player2.getAvatarUrl())
                .playerScore(score2)
                .eloChange(player2.getElo() - oldElo2)
                .correctAnswersCount(match.getPlayer2Attempt().getCorrectAnswersCount())
                .elo(player2.getElo())
                .duration(match.getPlayer2Attempt().getTimeSpent())
                .totalQuestions(match.getExam().getTotalQuestions())
                .WinStreak(player2.getWinStreak())
                .badgeRank(badgeResponse2)
                .build();
        checkMissionExam(match);
        return MatchResultResponse.builder()
                .matchId(match.getId())
                .winnerId(winnerId)
                .player1Id(player1.getId())
                .player2Id(player2.getId())
                .player1(p1Result)
                .player2(p2Result)
                .status("FINISHED")
                .build();
    }
    public String getPlayer1Username(Integer matchId) {
        PvpMatch match = pvpMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return match.getPlayer1().getUsername();
    }

    public String getPlayer2Username(Integer matchId) {
        PvpMatch match = pvpMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        return match.getPlayer2().getUsername();
    }

    private void updateElo(User winner, User loser, int change) {
        if (winner.getElo() == null) winner.setElo(75);
        if (loser.getElo() == null) loser.setElo(75);
        winner.setElo(winner.getElo() + change);
        loser.setElo(Math.max(0, loser.getElo() - change));

        userService.updatePlayerElo(winner.getId(), change);
        userService.updatePlayerElo(loser.getId(), -change);
    }
        // Award badge based on current elo (every 100 points)
        private Badge awardBadgeIfEligible(User user) {

            int rankLevel = user.getElo() / 100; // integer division
            // Define badge names in order of ranks (starting from rank 0 = no badge)
            String[] badgeNames = new String[]{
                    "bronze_1.0",
                    "bronze_2.0",
                    "bronze_3.0",
                    "silver_1.0",
                    "silver_2.0",
                    "silver_3.0",
                    "gold_1.0",
                    "gold_2.0",
                    "gold_3.0",
                    "challenger_0.0"
            };
            // Clamp index to max badge
            int idx = Math.min(rankLevel, badgeNames.length - 1);
            String badgeName = badgeNames[idx];
            // Find badge entity
            System.out.println(badgeName);
           return badgeRepository.findByBadgeName(badgeName);
        }
    }



