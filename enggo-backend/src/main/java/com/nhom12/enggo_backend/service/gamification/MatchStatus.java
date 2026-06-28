package com.nhom12.enggo_backend.service.gamification;

public enum MatchStatus {
    PENDING_ACCEPT, // Đang chờ 2 người bấm Chấp nhận
    PLAYING,        // Cả 2 đã đồng ý và đang làm bài
    FINISHED,       // Trận đấu kết thúc
    CANCELLED       // Bị hủy do có người không bấm Chấp nhận
}
