package com.nhom12.enggo_backend.entity.gamification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "missions")
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, length = 255)
    String title;

    @Column(columnDefinition = "text")
    String description;

    @Column(name = "reward_exp")
    Integer rewardExp;

    @Column(name = "mission_type", length = 50)
    String missionType;

    @Column(name = "target_value", nullable = false)
    Integer targetValue;

    @Column(name = "mission_key", nullable = false, length = 50)
    String missionKey;

    @Column(name = "time_limit_hours")
    Integer timeLimitHours;

    @Column(length = 50)
    String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;
}
