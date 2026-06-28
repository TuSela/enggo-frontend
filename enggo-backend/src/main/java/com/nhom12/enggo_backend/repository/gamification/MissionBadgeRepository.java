package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.Mission;
import com.nhom12.enggo_backend.entity.gamification.MissionBadge;
import com.nhom12.enggo_backend.entity.gamification.MissionBadgeId;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

@Repository
public interface MissionBadgeRepository extends JpaRepository<MissionBadge, MissionBadgeId> {

    List<MissionBadge> findByMission(Mission mission);
}
