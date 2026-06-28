package com.nhom12.enggo_backend.service.gamification;

import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Quản lý streakDays của User: tăng streak khi user thực hiện
 * exam (luyện tập) hoặc pvp trong ngày.
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    private static final Logger log = LoggerFactory.getLogger(StreakService.class);

    private final UserRepository userRepository;

    /**
     * Ghi nhận hoạt động (exam/pvp) cho user trong ngày hôm nay và cập nhật streak.
     * Idempotent trong cùng 1 ngày: gọi nhiều lần trong ngày không làm streak tăng thêm.
     *
     * Lưu ý: hàm này chỉ cập nhật field trên entity đã được quản lý (managed) trong
     * transaction hiện tại; không tự save lại để tránh ghi đè các thay đổi khác
     * đang được set trên cùng entity ở service gọi nó (vd exp, level, elo...).
     * Người gọi (caller) chịu trách nhiệm save user sau đó (qua userRepository.save
     * hoặc nhờ Hibernate dirty-checking trong transaction).
     */
    @Transactional
    public void recordDailyActivity(User user) {
        if (user == null) {
            return;
        }
        int before = user.getStreakDays() == null ? 0 : user.getStreakDays();
        user.touchStreak(LocalDate.now());
        int after = user.getStreakDays() == null ? 0 : user.getStreakDays();

        if (after != before) {
            log.debug("Streak updated for user {}: {} -> {}", user.getUsername(), before, after);
        }
    }

    /**
     * Overload tiện dụng khi chỉ có userId (vd gọi từ nơi chưa load sẵn User).
     */
    @Transactional
    public void recordDailyActivity(Integer userId) {
        if (userId == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot record streak: user {} not found", userId);
            return;
        }
        recordDailyActivity(user);
        userRepository.save(user);
    }
}
