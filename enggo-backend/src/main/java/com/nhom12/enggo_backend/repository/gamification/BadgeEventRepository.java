package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.BadgeEvent;
import com.nhom12.enggo_backend.entity.gamification.BadgeEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeEventRepository extends JpaRepository<BadgeEvent, BadgeEventId> {
}
