package com.nhom12.enggo_backend.dto.response.exam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "questionType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MultipleTypeResponse.class, name = "MULTIPLE_CHOICE"),
        @JsonSubTypes.Type(value = MatchingTypeResponse.class, name = "MATCHING"),
        @JsonSubTypes.Type(value = FillBlankTypeResponse.class, name = "FILL_BLANK")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QuestionDisplayResponse {
    Integer id;
    String questionType;
    String content;
    String attachmentUrl;
}
