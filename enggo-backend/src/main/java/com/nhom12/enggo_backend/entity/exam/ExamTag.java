package com.nhom12.enggo_backend.entity.exam;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exam_tags")
public class ExamTag {
    @EmbeddedId
    ExamTagId id;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @MapsId("examId")
    Exam exam;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    @MapsId("skillId")
    Skill skill;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    @MapsId("themeId")
    Theme theme;

}
