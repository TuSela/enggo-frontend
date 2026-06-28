package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam,Integer> {
    @Query(value = "SELECT * FROM exams WHERE is_active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Exam> findRandomExam();
    @Query("""
    SELECT DISTINCT e FROM Exam e
    LEFT JOIN FETCH e.examQuestions eq
    LEFT JOIN FETCH eq.question
    WHERE e.id = :id
""")
    Optional<Exam> findByIdWithQuestions(@Param("id") Integer id);

    @Query("""
    SELECT DISTINCT e FROM Exam e
    LEFT JOIN FETCH e.examTags et
    LEFT JOIN FETCH et.theme
    LEFT JOIN FETCH et.skill
    WHERE e.id = :id
""")
    Optional<Exam> findByIdWithTags(@Param("id") Integer id);

    @Query("SELECT e.id FROM Exam e")
    Page<Integer> findAllIds(Pageable pageable);

    @Query("""
            SELECT e.id FROM Exam e
            LEFT JOIN e.examTags t
            WHERE (:themeIds IS NULL OR t.theme.id IN :themeIds)
            AND (:skillIds IS NULL OR t.skill.id IN :skillIds)
            AND (:diffs IS NULL OR e.difficulty IN :diffs)
            GROUP BY e.id, e.createdAt
            """)
    Page<Integer> findIdsByFilter(
            Pageable pageable,
            @Param("themeIds") List<Integer> themeIds,
            @Param("skillIds") List<Integer> skillIds,
            @Param("diffs") List<Byte> diffs
    );

    @Query("""
            SELECT DISTINCT e FROM Exam e
            LEFT JOIN FETCH e.examTags et
            LEFT JOIN FETCH et.theme
            LEFT JOIN FETCH et.skill
            WHERE e.id IN :ids
            ORDER BY e.createdAt
            """)
    List<Exam> findByIds (@Param("ids") List<Integer> ids);

    @Query("""
            SELECT DISTINCT e FROM Exam e
            LEFT JOIN FETCH e.examTags et
            LEFT JOIN FETCH et.theme
            LEFT JOIN FETCH et.skill
            WHERE e.id IN :id
            """)
    Optional<Exam> findByIdWithTags(@Param("id") List<Integer> ids);

    @Query("""
            SELECT DISTINCT e FROM Exam e
            JOIN e.examTags t
            JOIN e.examQuestions eq
            WHERE t.theme.id IN :themeIds
            AND e.totalQuestions = :totalQuestions
            AND e.active = true
            
            AND e.id NOT IN (
                SELECT eq.exam.id FROM ExamQuestion eq
                WHERE eq.question.questionType NOT IN :questionTypes
            )
            
            AND e.id NOT IN (
                SELECT et.exam.id FROM ExamTag et
                WHERE et.theme.id NOT IN :themeIds
            )
            
            AND e.id NOT IN (
                SELECT ea.exam.id FROM ExamAttempt ea
                WHERE ea.user.id = :userId
            )
            """)
    List<Exam> findAvailableExamsByThemesAndType (
            @Param("themeIds") List<Integer> themeIds,
            @Param("totalQuestions") int totalQuestions,
            @Param("questionTypes") List<String> questionTypes,
            @Param("userId") Integer userId
    );

    @Query("""
            SELECT DISTINCT e FROM Exam e
            JOIN e.examTags t
            JOIN e.examQuestions eq
            WHERE t.theme.id IN :themeIds
            AND e.totalQuestions = :totalQuestions
            AND e.active = true
            
            AND e.id NOT IN (
                SELECT eq.exam.id FROM ExamQuestion eq
                WHERE eq.question.questionType NOT IN :questionTypes
            )
            
            AND e.id NOT IN (
                SELECT et.exam.id FROM ExamTag et
                WHERE et.theme.id NOT IN :themeIds
            )
            """)
    List<Exam> findAnyAvailableExams (
            @Param("themeIds") List<Integer> themeIds,
            @Param("totalQuestions") int totalQuestions,
            @Param("questionTypes") List<String> questionTypes
    );
}
