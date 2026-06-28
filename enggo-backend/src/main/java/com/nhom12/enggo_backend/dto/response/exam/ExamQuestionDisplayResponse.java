package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;

@Data
public class ExamQuestionDisplayResponse {
    Integer orderPriority;
    QuestionDisplayResponse question;
}
