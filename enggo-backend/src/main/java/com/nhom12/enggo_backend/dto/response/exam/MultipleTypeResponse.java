package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class MultipleTypeResponse extends QuestionDisplayResponse {
    List<MultipleOptionResponse> multipleOptions;
}
