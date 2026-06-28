package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MissionProgressRepository extends JpaRepository<MissionProgress, Integer> {
    // Find unclaimed (not CLAIMED) progress whose deadline is before a given cutoff (e.g., yesterday)
    List<MissionProgress> findByUserIdAndDeadlineBeforeAndStatusNot(Integer userId, java.time.LocalDateTime cutoff, String status);
    // Find progress for a user within a date range (today's missions)
    List<MissionProgress> findByUserIdAndDeadlineBetween(Integer userId, LocalDateTime start,LocalDateTime end);

    // Delete old progress entries whose deadline has passed
    void deleteByDeadlineBefore(java.time.LocalDateTime cutoff);

    java.util.Optional<MissionProgress> findByUserIdAndMissionId(Integer userId, Integer missionId);

    List<MissionProgress> findByUserIdAndDeadlineBetweenAndMissionMissionType(
            Integer userId,
            LocalDateTime start,
            LocalDateTime end,
            String missionType
    );

}
