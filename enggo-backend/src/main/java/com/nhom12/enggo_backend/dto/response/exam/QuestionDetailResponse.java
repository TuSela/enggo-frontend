package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({
        "id", "themes", "skills", "content", "attachmentUrl", "difficulty",
        "explanation", "createdBy", "options", "createdAt", "updatedAt"
})
public class QuestionDetailResponse {
    Integer id;
    String questionType;
    String content;
    String explanation;
    Byte difficulty;
    String attachmentUrl;
    List<ThemeResponse> themes;
    List<SkillResponse> skills;
    UserMinimalResponse createdBy;
    List<OptionResponse> options;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
