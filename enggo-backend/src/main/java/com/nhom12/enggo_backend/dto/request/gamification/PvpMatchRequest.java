package com.nhom12.enggo_backend.dto.request.gamification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PvpMatchRequest {
    Integer player1Id;
    Integer player1AttemptId;
    Integer player2Id;
    Integer player2AttemptId;
    Integer examId;
    Integer player1Score;
    Integer player2Score;
    Integer winnerId;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
}
