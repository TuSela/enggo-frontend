package com.nhom12.enggo_backend.dto.request.exam;

import lombok.Data;

@Data
public class FillBlankSubmitRequest {
    Integer blankId;
    Integer position;
    String userInput;
}
