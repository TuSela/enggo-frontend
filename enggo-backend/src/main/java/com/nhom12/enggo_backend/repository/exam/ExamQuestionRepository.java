package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion,Long> {
}
