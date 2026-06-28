package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.UserBadge;
import com.nhom12.enggo_backend.entity.gamification.UserBadgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {
    List<UserBadge> findAllByUser_Username(String username);
}
