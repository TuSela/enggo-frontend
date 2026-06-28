package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    boolean existsBySkillName(String skillName);
    Skill findBySkillName(String skillName);
}
