package com.nhom12.enggo_backend.dto.response.exam;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionResponse {
    Integer id;
    @NotNull
    @NotEmpty
    String content;

    @NotNull
    Byte difficulty;

    List<ThemeResponse> themes;
    List<SkillResponse> skills;
}
