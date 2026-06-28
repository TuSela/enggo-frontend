package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionResponse {
    QuestionDetailResponse questionDetails;
    Integer orderPriority;
}
