package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.response.exam.*;
import com.nhom12.enggo_backend.dto.response.gamification.ExamPvpDisplayResponse;
import com.nhom12.enggo_backend.entity.exam.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface ExamMapper {
    default List<ThemeResponse> mapThemes(List<ExamTag> examTags) {
        if (examTags == null || examTags.isEmpty()) {
            return Collections.emptyList();
        }

        return examTags.stream()
                .map(t-> new ThemeResponse(
                        t.getTheme().getId(),
                        t.getTheme().getThemeName(),t.getTheme().getThemeName()))
                .distinct().toList();
    }

    default List<SkillResponse> mapSkills(List<ExamTag> examTags) {
        if (examTags == null || examTags.isEmpty()) {
            return Collections.emptyList();
        }

        return examTags.stream()
                .map(s -> new SkillResponse(
                        s.getSkill().getId(),
                        s.getSkill().getSkillName()))
                .distinct().toList();
    }

    @Mapping(target="createdBy.username", source = "createdBy.username")
    @Mapping(target = "createdBy.avatarUrl", source = "createdBy.avatarUrl")
    @Mapping(target = "themes", expression = "java(mapThemes(exam.getExamTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(exam.getExamTags()))")
    @Mapping(target = "questions", source = "examQuestions")
    ExamDetailResponse toExamDetailResponse(Exam exam);

    @Mapping(target = "questionDetails", source = "question")
    @Mapping(target = "orderPriority", source = "orderPriority")
    ExamQuestionResponse mapExamQuestion(ExamQuestion eq);

    @Mapping(target = "themes", expression = "java(mapThemes(exam.getExamTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(exam.getExamTags()))")
    ExamResponse toExamResponse(Exam exam);

    List<ExamResponse> toExamResponses(List<Exam> exams);

    @Mapping(target = "questions", source = "exam.examQuestions")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "attemptId", source = "examAttempt.id")
    ExamDisplayResponse toExamDisplayResponse(Exam exam,ExamAttempt examAttempt);

    @Mapping(target = "questions", source = "exam.examQuestions")
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "attemptId1", source = "examAttempt.id")
    @Mapping(target = "attemptId2", source = "examAttempt2.id")
    ExamPvpDisplayResponse toExamPvpDisplayResponse(Exam exam, ExamAttempt examAttempt,ExamAttempt examAttempt2);

    @Mapping(target = "orderPriority", source = "orderPriority")
    @Mapping(target = "question", source = "question")
    ExamQuestionDisplayResponse toExamQuestionDisplayResponse(ExamQuestion examQuestion);

    default QuestionDisplayResponse toQuestionDisplayResponse(Question question) {
        if  (question == null) {
            return null;
        }

        var response = switch (question.getQuestionType()) {
            case "MATCHING" -> toMatchingTypeResponse(question.getOptions());
            case "FILL_BLANK" -> toFillBlankTypeResponse(question.getOptions());
            default -> toMultipleTypeResponse(question.getOptions());
        };

        if (response != null) {
            response.setId(question.getId());
            response.setContent(question.getContent());
            response.setQuestionType(question.getQuestionType());
            response.setAttachmentUrl(question.getAttachmentUrl());
        }

        return response;
    }

    MultipleOptionResponse toMultipleOptionResponse(QuestionOption questionOption);

    default MultipleTypeResponse toMultipleTypeResponse (List<QuestionOption> options) {
        if  (options == null || options.isEmpty()) {
            return null;
        }

        var response = new MultipleTypeResponse();
        response.setMultipleOptions(
                options.stream()
                        .map(this::toMultipleOptionResponse)
                        .toList()
        );

        return response;
    };

    default FillBlankOptionResponse toFillBlankOptionResponse(QuestionOption option) {
        if (option == null) return null;

        var response = new FillBlankOptionResponse();
        response.setBlankId(option.getId());
        response.setPosition(
                Integer.parseInt(option.getOption_group().replace("BLANK_", ""))
        );
        response.setPlaceholder(null);
        response.setMaxLength(option.getOptionText().length());
        return response;
    }

    default FillBlankTypeResponse toFillBlankTypeResponse (List<QuestionOption> options) {
        if   (options == null || options.isEmpty()) {
            return null;
        }

        var response = new  FillBlankTypeResponse();
        response.setFillBlankOptions(
                options.stream()
                        .filter(o -> o.getOption_group() != null && o.getOption_group().startsWith("BLANK_"))
                        .sorted(Comparator.comparing(o -> Integer.parseInt(o.getOption_group().replace("BLANK_", ""))))
                        .map(this::toFillBlankOptionResponse)
                        .toList()
        );

        return response;
    };


    default MatchingTypeResponse toMatchingTypeResponse(List<QuestionOption> options) {
        if  (options == null || options.isEmpty()) {
            return null;
        }

        var response = new MatchingTypeResponse();
        response.setLeftOptions(
                options.stream()
                        .filter(o -> "LEFT".equals(o.getOption_group()))
                        .map(this::toMultipleOptionResponse)
                        .toList()
        );

        List<MultipleOptionResponse> right = new java.util.ArrayList<>(options.stream()
                .filter(o -> "RIGHT".equals(o.getOption_group()))
                .map(this::toMultipleOptionResponse)
                .toList());

        Collections.shuffle(right);
        response.setRightOptions(right);

        return response;
    }
}
