package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamAttemptDetailResponse {
    Integer orderPriority;
    BigDecimal score;
    QuestionResultResponse question;
}
