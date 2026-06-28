package com.nhom12.enggo_backend.entity.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class QuestionTagId implements Serializable {
    @Column(name = "question_id")
    Integer questionId;

    @Column(name = "theme_id")
    Integer themeId;

    @Column(name = "skill_id")
    Integer skillId;
}
