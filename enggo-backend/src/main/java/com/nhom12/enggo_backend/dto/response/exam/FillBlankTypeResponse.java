package com.nhom12.enggo_backend.dto.response.exam;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FillBlankTypeResponse extends QuestionDisplayResponse{
    List<FillBlankOptionResponse> fillBlankOptions;
}
