package com.nhom12.enggo_backend.dto.request.exam;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreationRequest {
    String questionType;
    @NotNull
    @NotEmpty
    String content;

    @NotNull
    @NotEmpty
    String explanation;

    @NotNull
    Byte difficulty;

    String attachmentUrl;

    List<OptionRequest> options;
    List<FillBlankOptionRequest> blankAnswers;

    List<Integer> themeIds;
    List<Integer> skillIds;
}
