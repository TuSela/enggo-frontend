package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<QuestionTag,Long> {
    void deleteByQuestionId(Integer questionId);

    @Query("""
        SELECT DISTINCT qt.id.themeId
        FROM QuestionTag qt
        JOIN ExamQuestion eq ON qt.id.questionId = eq.id.questionId
        WHERE eq.exam.id = :id
    """)
    List<Integer> findDistinctThemeIdsByExamId(@Param("id") Integer id);

    @Query("""
        SELECT DISTINCT qt.id.skillId FROM QuestionTag qt
        JOIN ExamQuestion eq ON qt.id.questionId = eq.id.questionId
        WHERE eq.exam.id = :id
    """)
    List<Integer> findDistinctSkillIdsByExamId(@Param("id") Integer id);
}
