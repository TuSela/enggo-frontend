package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class FillBlankOptionResponse {
    Integer blankId;
    Integer position;
    String placeholder;
    Integer maxLength;
}
