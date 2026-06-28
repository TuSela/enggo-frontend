package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResultResponse {
    Integer id;
    String questionType;
    String content;
    List<MultipleOptionResultResponse> multipleOptions;
    List<FillBlankResultResponse> fillBlankOptions;
    List<MatchingResultResponse> matchingResults;
    String explanation;
}
