package com.nhom12.enggo_backend.entity.exam;

import com.nhom12.enggo_backend.dto.response.exam.ExamDetailResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "exam_attempts")
public class ExamAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Column(name = "total_score", precision = 5, scale = 2)
    BigDecimal totalScore;

    @Column(name = "correct_answers_count")
    Integer correctAnswersCount;

    @Column(name = "started_at", insertable = false, updatable = false)
    LocalDateTime startedAt;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @Column(name = "is_complete")
    Boolean complete;

    @Column(name = "exp_gained")
    Integer expGained;

    @Column(name = "bonus_exp")
    Integer bonusExp;

    @Column(name = "time_spent")
    String timeSpent;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExamAttemptDetail> examAttemptDetails = new ArrayList<>();
}
