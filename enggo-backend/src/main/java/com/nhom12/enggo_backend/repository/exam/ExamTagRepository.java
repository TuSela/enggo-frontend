package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.ExamTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamTagRepository extends JpaRepository<ExamTag, Integer> {
    void deleteByExamId(Integer id);
}
