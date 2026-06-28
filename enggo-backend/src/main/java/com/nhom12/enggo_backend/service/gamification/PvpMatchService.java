package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.dto.request.gamification.PvpMatchRequest;
import com.nhom12.enggo_backend.dto.response.exam.ExamDisplayResponse;
import com.nhom12.enggo_backend.dto.response.gamification.PvpMatchResponse;
import com.nhom12.enggo_backend.entity.exam.Exam;
import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.gamification.PvpMatch;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.exception.AppException;
import com.nhom12.enggo_backend.exception.ErrorCode;
import com.nhom12.enggo_backend.mapper.exam.ExamMapper;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.exam.ExamAttemptRepository;
import com.nhom12.enggo_backend.repository.exam.ExamRepository;
import com.nhom12.enggo_backend.repository.gamification.PvpMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PvpMatchService {
    private final PvpMatchRepository pvpMatchRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    @Autowired
    private ExamMapper examMapper;

    @Transactional(readOnly = true)
    public List<PvpMatchResponse> getPvpMatches() {
        return pvpMatchRepository.findAll().stream().map(this::toPvpMatchResponse).toList();
    }

    @Transactional(readOnly = true)
    public PvpMatchResponse getPvpMatch(Integer id) {
        return toPvpMatchResponse(findPvpMatch(id));
    }


    public PvpMatchResponse updatePvpMatch(Integer id, PvpMatchRequest request) {
        PvpMatch pvpMatch = findPvpMatch(id);
        applyPvpMatchRequest(pvpMatch, request);
        return toPvpMatchResponse(pvpMatchRepository.save(pvpMatch));
    }

    private void applyPvpMatchRequest(PvpMatch pvpMatch, PvpMatchRequest request) {
        pvpMatch.setPlayer1(findUser(request.getPlayer1Id()));
        pvpMatch.setPlayer1Attempt(findExamAttemptOrNull(request.getPlayer1AttemptId()));
        pvpMatch.setPlayer2(findUser(request.getPlayer2Id()));
        pvpMatch.setPlayer2Attempt(findExamAttemptOrNull(request.getPlayer2AttemptId()));
        pvpMatch.setExam(findExam(request.getExamId()));
        pvpMatch.setPlayer1Score(request.getPlayer1Score());
        pvpMatch.setPlayer2Score(request.getPlayer2Score());
        pvpMatch.setWinner(findUserOrNull(request.getWinnerId()));
        pvpMatch.setStatus(request.getStatus());
        pvpMatch.setStartTime(request.getStartTime());
        pvpMatch.setEndTime(request.getEndTime());
    }

    private PvpMatchResponse toPvpMatchResponse(PvpMatch pvpMatch) {
        User winner = pvpMatch.getWinner();
        ExamAttempt player1Attempt = pvpMatch.getPlayer1Attempt();
        ExamAttempt player2Attempt = pvpMatch.getPlayer2Attempt();
        return PvpMatchResponse.builder()
                .id(pvpMatch.getId())
                .player1Id(pvpMatch.getPlayer1().getId())
                .player1Username(pvpMatch.getPlayer1().getUsername())
                .player1AttemptId(player1Attempt != null ? player1Attempt.getId() : null)
                .player2Id(pvpMatch.getPlayer2().getId())
                .player2Username(pvpMatch.getPlayer2().getUsername())
                .player2AttemptId(player2Attempt != null ? player2Attempt.getId() : null)
                .examId(pvpMatch.getExam().getId())
                .examTitle(pvpMatch.getExam().getTitle())
                .player1Score(pvpMatch.getPlayer1Score())
                .player2Score(pvpMatch.getPlayer2Score())
                .winnerId(winner != null ? winner.getId() : null)
                .winnerUsername(winner != null ? winner.getUsername() : null)
                .status(pvpMatch.getStatus())
                .startTime(pvpMatch.getStartTime())
                .endTime(pvpMatch.getEndTime())
                .createdAt(pvpMatch.getCreatedAt())
                .build();
    }

    private PvpMatch findPvpMatch(Integer id) {
        return pvpMatchRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUser(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private User findUserOrNull(Integer id) {
        return id == null ? null : findUser(id);
    }

    private Exam findExam(Integer id) {
        return examRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private ExamAttempt findExamAttemptOrNull(Integer id) {
        return id == null ? null : examAttemptRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    public ExamDisplayResponse startPvP (Integer examId) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

        var exam = examRepository.findById(examId).orElseThrow(RuntimeException::new);

        if (!exam.getActive()) {
            throw new IllegalStateException("Exam has been stopped");
        }

        boolean hasOngoing = examAttemptRepository.existsByUserIdAndCompleteFalse(user.getId());

        if (hasOngoing) {
            throw new IllegalStateException("You have an ongoing attempt");
        }
        ExamAttempt attempt = ExamAttempt.builder()
                .user(user)
                .exam(exam)
                .complete(false)
                .startedAt(LocalDateTime.now())
                .build();
        examAttemptRepository.save(attempt);
        return examMapper.toExamDisplayResponse(exam,attempt);
    }
}
