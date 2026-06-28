package com.nhom12.enggo_backend.dto.request.exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class OptionRequest {
    String optionText;
    @JsonProperty("isCorrect")
    boolean correct;
    String optionGroup;
    String matchKey;
}
