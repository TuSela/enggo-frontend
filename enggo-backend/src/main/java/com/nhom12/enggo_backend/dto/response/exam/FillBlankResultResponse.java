package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class FillBlankResultResponse {
    Integer blankId;
    Integer position;
    Integer maxLength;
    String userInput;
    String correctValue;
    Boolean isCorrect;
}
