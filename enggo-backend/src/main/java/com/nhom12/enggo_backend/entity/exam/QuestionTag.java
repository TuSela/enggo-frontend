package com.nhom12.enggo_backend.entity.exam;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "question_tags")
public class QuestionTag {
    @EmbeddedId
    QuestionTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("themeId")
    @JoinColumn(name = "theme_id", nullable = false)
    Theme theme;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    Skill skill;
}
