package com.nhom12.enggo_backend.dto.request.exam;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RandomBlueprintRequest {
    Byte difficulty;
    List<String> questionTypes = new ArrayList<>(List.of("MULTIPLE_CHOICE", "FILL_BLANK", "MATCHING"));
    List<Integer> themeIds;
    int totalQuestions;
}
