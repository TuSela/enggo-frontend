package com.nhom12.enggo_backend.entity.studygroup;

import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import com.nhom12.enggo_backend.entity.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "assignment_progress")
public class AssignmentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    GroupAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "progress_status", length = 50)
    String progressStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_attempt_id")
    ExamAttempt lastAttempt;

    @Column(name = "highest_score", precision = 5, scale = 2)
    BigDecimal highestScore;

    @Column(name = "updated_at", insertable = false, updatable = false)
    LocalDateTime updatedAt;
}
