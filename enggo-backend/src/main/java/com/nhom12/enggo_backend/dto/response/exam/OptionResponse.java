package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionResponse {
    Integer id;
    String optionText;
    Boolean isCorrect;
    String optionGroup;
    String matchKey;
}
