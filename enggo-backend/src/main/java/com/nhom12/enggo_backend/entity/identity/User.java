package com.nhom12.enggo_backend.entity.identity;

import com.nhom12.enggo_backend.entity.gamification.Badge;
import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionProgress;
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, unique = true, length = 50)
    String username;

    @Column(name = "full_name")
    String fullName;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    @Column(nullable = false, length = 255)
    String password;

    @Column(name = "exp")
    Integer exp;

    Integer level;

    @Column(name = "streak_days")
    Integer streakDays;

    @Column(name = "last_streak_date")
    LocalDateTime lastStreakDate;

    @Column(name = "completed_tasks")
    Integer completedTasks;

    @Column(name = "pvp_wins")
    Integer pvpWins;

    @Column(name = "elo")
    Integer elo;

    @Column(name = "pending_invites")
    Integer pendingInvites;

    @Column(name = "avatar_url", length = 255)
    String avatarUrl;

    @Column(length = 50)
    String status;

    @Column(columnDefinition = "text")
    String bio;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "win_streak")
    Integer winStreak;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_rank", nullable = false)
    Badge badgeRank;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    Set<Role> roles;

    public void incrementWinStreak() {
        this.winStreak = this.winStreak + 1;
    }
    public void resetWinStreak(){
        this.winStreak = 0;
    }

    /**
     * Cập nhật streakDays dựa trên ngày hoạt động (exam/pvp) gần nhất.
     * - Lần đầu hoạt động: streakDays = 1
     * - Hoạt động vào ngày kế tiếp ngày trước đó: streakDays + 1
     * - Hoạt động lại trong cùng 1 ngày: không thay đổi
     * - Bỏ lỡ >= 1 ngày: reset về 1
     */
    public void touchStreak(java.time.LocalDate today) {
        if (this.lastStreakDate == null) {
            this.streakDays = 1;
        } else {
            java.time.LocalDate lastDate = this.lastStreakDate.toLocalDate();
            long diff = java.time.temporal.ChronoUnit.DAYS.between(lastDate, today);
            if (diff == 0) {
                return; // đã tính hôm nay rồi
            } else if (diff == 1) {
                this.streakDays = (this.streakDays == null ? 0 : this.streakDays) + 1;
            } else {
                this.streakDays = 1;
            }
        }
        this.lastStreakDate = today.atStartOfDay();
    }

    /**
     * Add experience points to the user.
     */
    public void addExp(int amount) {
        if (this.exp == null) {
            this.exp = 0;
        }
        this.exp += amount;
    }

}
