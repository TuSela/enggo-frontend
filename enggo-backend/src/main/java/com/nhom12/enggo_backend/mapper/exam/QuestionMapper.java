package com.nhom12.enggo_backend.mapper.exam;

import com.nhom12.enggo_backend.dto.request.exam.*;
import com.nhom12.enggo_backend.dto.response.exam.*;
import com.nhom12.enggo_backend.entity.exam.Question;
import com.nhom12.enggo_backend.entity.exam.QuestionOption;
import com.nhom12.enggo_backend.entity.exam.QuestionTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    Question toQuestion(QuestionCreationRequest request);

    @Mapping(target="createdBy.username", source = "createdBy.username")
    @Mapping(target = "createdBy.avatarUrl", source = "createdBy.avatarUrl")
    @Mapping(target = "themes", expression = "java(mapThemes(question.getTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(question.getTags()))")
    QuestionDetailResponse toQuestionDetailResponse(Question question);

    List<QuestionOption> toQuestionOptions(List<OptionRequest> requests);

    List<QuestionOption> toEntity(List<OptionUpdateRequest> requests);

    QuestionOption toQuestionOption(OptionRequest request);

    QuestionOption toEntity(OptionUpdateRequest request);

    @Mapping(target = "isCorrect", source = "correct")
    @Mapping(target = "optionGroup", source = "option_group")
    @Mapping(target = "matchKey", source = "match_key")
    OptionResponse toOptionResponse(QuestionOption option);

    @Mapping(target = "themes", expression = "java(mapThemes(question.getTags()))")
    @Mapping(target = "skills", expression = "java(mapSkills(question.getTags()))")
    QuestionResponse toQuestionResponse(Question question);

    List<QuestionResponse> toQuestionResponses(List<Question> questions);

    default List<ThemeResponse> mapThemes(List<QuestionTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        return tags.stream()
                .map(t -> new ThemeResponse(
                        t.getTheme().getId(),
                        t.getTheme().getThemeName(),t.getTheme().getThemeImage()))
                .distinct().toList();
    }

    default List<SkillResponse> mapSkills(List<QuestionTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        return tags.stream()
                .map(t -> new SkillResponse(
                        t.getSkill().getId(),
                        t.getSkill().getSkillName()))
                .distinct().toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "options", ignore = true)
    void updateQuestion(@MappingTarget Question question, QuestionUpdateRequest request);

    @Mapping(target = "correct", source = "correct")
    void updateOption(@MappingTarget QuestionOption option, OptionUpdateRequest request);

    Question toQuestion(QuestionDetailResponse response);

    default List<QuestionOption> mapFillBlankOption(List<FillBlankOptionRequest> blanks, Question question) {
        if (blanks == null || blanks.isEmpty()) {
            return Collections.emptyList();
        }

        return blanks.stream()
                .map(b -> QuestionOption.builder()
                        .question(question)
                        .optionText(b.getCorrectValue())
                        .correct(true)
                        .option_group("BLANK_" + b.getPosition())
                        .build()
                ).toList();
    }

    default List<QuestionOption> mapMatchingQuestion(List<OptionRequest> requests, Question question) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(r -> QuestionOption.builder()
                        .question(question)
                        .optionText(r.getOptionText())
                        .correct(r.isCorrect())
                        .option_group(r.getOptionGroup())
                        .match_key(r.getMatchKey())
                        .build()
                ).toList();
    }
}
