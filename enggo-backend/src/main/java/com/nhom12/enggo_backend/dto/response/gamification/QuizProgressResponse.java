package com.nhom12.enggo_backend.dto.response.gamification;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class QuizProgressResponse {
    private Integer userId;       // ID c?a ngu?i v?a tr? l?i
    private Integer currentScore; // T?ng di?m hi?n t?i c?a ngu?i d� sau khi c?ng di?m c�u n�y
    private Boolean isCorrect;    // K?t qu? c�u v?a tr? l?i (d�ng hay sai) d? hi?n th? animation/m�u s?c
    private Integer questionId;
}
