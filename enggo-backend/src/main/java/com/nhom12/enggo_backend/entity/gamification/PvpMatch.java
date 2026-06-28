package com.nhom12.enggo_backend.entity.gamification;

import com.nhom12.enggo_backend.entity.exam.Exam;
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

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "pvp_matches")
public class PvpMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_attempt_id")
    ExamAttempt player1Attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    User player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_attempt_id")
    ExamAttempt player2Attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    Integer player1Score;

    Integer player2Score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    User winner;

    @Column(length = 50)
    String status;

    LocalDateTime startTime;

    LocalDateTime endTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name ="host_user_id")
    Integer hostUserId;



}
