package com.nhom12.enggo_backend.scheduler;

import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionProgressRepository;
import com.nhom12.enggo_backend.repository.gamification.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler that runs nightly to generate daily missions for every user and clean up expired progress.
 * The job runs at 00:00 server time.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyMissionScheduler {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final MissionProgressRepository missionProgressRepository;

    // Number of missions per user per day – configurable via application.yml (fallback to 3)
    private static final int MISSIONS_PER_USER = 3;

    @Scheduled(cron = "0 0 0 * * *") // midnight every day
    @Transactional
    public void generateDailyMissions() {
        log.info("=== Starting daily mission generation ===");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        // 1. Cleanup old progress entries whose deadline has passed
        missionProgressRepository.deleteByDeadlineBefore(now);
        log.info("Deleted expired mission progress entries before {}", now);

        // 2. Fetch all users
        List<User> users = userRepository.findAll();
        log.info("Generating daily missions for {} users", users.size());

        // 3. For each user, pick random missions and create progress records
        for (User user : users) {
            // Fetch random missions (LIMIT = MISSIONS_PER_USER)
            List<Mission> randomMissions = missionRepository.findRandomMissions(PageRequest.of(0, MISSIONS_PER_USER));
            for (Mission mission : randomMissions) {
                MissionProgress progress = MissionProgress.builder()
                        .user(user)
                        .mission(mission)
                        .currentValue(0)
                        .status("IN_PROGRESS")
                        .deadline(tomorrow) // expires at the end of the day
                        .build();
                missionProgressRepository.save(progress);
            }
        }
        log.info("=== Daily mission generation completed ===");
    }
}
