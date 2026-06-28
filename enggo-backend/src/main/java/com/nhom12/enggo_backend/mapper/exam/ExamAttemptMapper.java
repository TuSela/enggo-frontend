package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.ExamAnswerRequest;
import com.nhom12.enggo_backend.dto.request.exam.ExamSubmitRequest;
import com.nhom12.enggo_backend.dto.request.exam.FillBlankSubmitRequest;
import com.nhom12.enggo_backend.dto.response.exam.*;
import com.nhom12.enggo_backend.entity.exam.*;
import com.nhom12.enggo_backend.service.exam.ScoreCheck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamAttemptMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attempt", source = "examAttempt")
    @Mapping(target = "question", source = "question")
    @Mapping(target = "selectedOption", source = "option")
    @Mapping(target = "userInput", source = "userInput")
    @Mapping(target = "correct", source = "result.correct")
    @Mapping(target = "score", source = "result.score")
    ExamAttemptDetail toExamAttemptDetail (
            ExamAttempt examAttempt,
            Question question,
            QuestionOption option,
            String userInput,
            ScoreCheck result);

    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "totalQuestions", source = "exam.totalQuestions")
    @Mapping(target = "expGained", source = "expGained")
    @Mapping(target = "bonusExp", source = "bonusExp")
    @Mapping(target = "timeSpent", source = "timeSpent")
    ExamSubmitResponse toExamSubmitResponse(ExamAttempt examAttempt);

    List<ExamSubmitResponse> toExamSubmitResponses(List<ExamAttempt> examAttempts);

    default MultipleOptionResultResponse toMultipleOptionResultResponse (ExamAttemptDetail detail, QuestionOption option) {
        Integer selectedId = detail.getSelectedOption() != null ? detail.getSelectedOption().getId() : null;

        MultipleOptionResultResponse response = new MultipleOptionResultResponse();
        response.setId(option.getId());
        response.setOptionText(option.getOptionText());
        response.setCorrect(option.isCorrect());
        response.setSelected(option.getId().equals(selectedId));

        return response;
    };
}
