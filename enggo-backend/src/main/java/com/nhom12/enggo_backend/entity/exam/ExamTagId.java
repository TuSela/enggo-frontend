package com.nhom12.enggo_backend.entity.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ExamTagId implements Serializable {
    @Column(name = "exam_id")
    Integer examId;

    @Column(name = "theme_id")
    Integer themeId;

    @Column(name = "skill_id")
    Integer skillId;
}
