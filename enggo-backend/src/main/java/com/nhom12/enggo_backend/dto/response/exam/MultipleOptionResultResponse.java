package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class MultipleOptionResultResponse {
    Integer id;
    String optionText;
    boolean isCorrect;
    boolean isSelected;
}
