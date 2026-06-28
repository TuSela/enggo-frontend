package com.nhom12.enggo_backend.dto.request.exam;

import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.exam.OptionResponse;
import com.nhom12.enggo_backend.dto.response.exam.SkillResponse;
import com.nhom12.enggo_backend.dto.response.exam.ThemeResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionUpdateRequest {
    String content;
    String explanation;
    Byte difficulty;
    String attachmentUrl;
    List<Integer> themeIds;
    List<Integer> skillIds;
    List<OptionUpdateRequest> options;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
