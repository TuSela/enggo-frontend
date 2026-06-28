package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer> {
    // Fetch a random list of missions (limited by pageable)
    @org.springframework.data.jpa.repository.Query(value = "SELECT m FROM Mission m ORDER BY FUNCTION('RAND')",
            countQuery = "SELECT count(m) FROM Mission m")
    java.util.List<Mission> findRandomMissions(org.springframework.data.domain.Pageable pageable);

}
