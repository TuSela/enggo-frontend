package com.nhom12.enggo_backend.dto.request.exam;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillRequest {
    @NotNull
    @NotEmpty
    String skillName;
    String skillDescription;
}
