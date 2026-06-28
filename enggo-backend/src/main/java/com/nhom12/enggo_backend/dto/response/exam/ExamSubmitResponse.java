package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamSubmitResponse {
    Integer attemptId;
    Integer examId;
    BigDecimal totalScore;
    Integer correctAnswersCount;
    Integer totalQuestions;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    Long timeTakenSeconds;
    String timeSpent;
    Integer expGained;
    Integer bonusExp;
    List<ExamAttemptDetailResponse> detail;
}
