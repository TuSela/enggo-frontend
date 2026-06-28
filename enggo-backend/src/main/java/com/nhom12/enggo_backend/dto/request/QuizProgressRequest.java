package com.nhom12.enggo_backend.dto.request;

import lombok.Data;

@Data
public class QuizProgressRequest {
        private Integer playerId;           // Ai là người vừa trả lời
        private Integer currentQuestionIndex;// Họ đang ở câu số mấy (ví dụ: câu 3/10)
    }

