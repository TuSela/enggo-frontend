package com.nhom12.enggo_backend.dto.request.exam;

import lombok.Data;

import java.util.List;

@Data
public class ExamAnswerRequest {
    Integer questionId;
    Integer selectedOptionId;
    List<FillBlankSubmitRequest> fillBlanks;
    List<MatchingSubmitRequest> matchings;
}
