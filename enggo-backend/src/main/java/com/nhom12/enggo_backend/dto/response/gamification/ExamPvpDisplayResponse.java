package com.nhom12.enggo_backend.dto.response.gamification;

import com.nhom12.enggo_backend.dto.response.exam.ExamQuestionDisplayResponse;
import lombok.Data;

import java.util.List;
@Data
public class ExamPvpDisplayResponse {
    Integer matchId;
    Integer examId;
    Integer attemptId1;
    Integer attemptId2;
    String title;
    String examType;
    Byte difficulty;
    Integer totalQuestions;
    Integer durationMinutes;
    List<ExamQuestionDisplayResponse> questions;
}
