package com.nhom12.enggo_backend.dto.response.exam;

import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.entity.exam.ExamQuestion;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDetailResponse {
    Integer id;
    String title;
    String examType;
    Byte difficulty;
    Integer totalQuestions;
    Integer durationMinutes;
    Boolean active;
    UserMinimalResponse createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ExamQuestionResponse> questions;
    List<ThemeResponse> themes;
    List<SkillResponse> skills;
}
