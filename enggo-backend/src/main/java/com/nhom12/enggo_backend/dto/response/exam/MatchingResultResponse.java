package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class MatchingResultResponse {
    Integer leftId;
    String leftText;
    Integer userRightId;
    String userRightText;
    Integer correctRightId;
    String correctRightText;
    Boolean isCorrect;
}
