package com.nhom12.enggo_backend.repository.exam;

import ch.qos.logback.core.pattern.color.BoldYellowCompositeConverter;
import com.nhom12.enggo_backend.entity.exam.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    boolean existsByContent(String request);

    @Query("SELECT q.id FROM Question q")
    Page<Integer> findAllIds(Pageable pageable);

    @Query("""
        SELECT q.id FROM Question q
        LEFT JOIN q.tags t
        WHERE (:themeIds IS NULL OR t.theme.id IN :themeIds)
        AND (:skillIds IS NULL OR t.skill.id IN :skillIds)
        AND (:diffs IS NULL OR q.difficulty IN :diffs)
        GROUP BY q.id, q.createdAt
    """)
    Page<Integer> findIdsByFilter(
            Pageable pageable,
            @Param("themeIds") List<Integer> themeIds,
            @Param("skillIds") List<Integer> skillIds,
            @Param("diffs") List<Byte> diffs);

    @Query("""
        SELECT DISTINCT q FROM Question q
        LEFT JOIN FETCH q.tags t
        LEFT JOIN FETCH t.theme
        LEFT JOIN FETCH t.skill
        WHERE q.id IN :ids
        ORDER BY q.createdAt
    """)
    List<Question> findByIds(@Param("ids") List<Integer> ids);

    @Query("""
        SELECT DISTINCT q FROM Question q
        LEFT JOIN FETCH q.tags t
        LEFT JOIN FETCH t.theme
        LEFT JOIN FETCH t.skill
        WHERE q.id = :id
    """)
    Optional<Question> findByIdWithTags(@Param("id") Integer id);

    @Query("""
            SELECT DISTINCT q FROM Question q
            JOIN q.tags t
            WHERE t.theme.id IN :themeIds
            AND q.questionType IN :questionTypes
            
            AND q.id NOT IN (
                SELECT q2.id FROM Question q2
                JOIN q2.tags t2
                WHERE t2.theme.id NOT IN :themeIds
            )
            """)
    List<Question> findQuestionsByThemesAndTypes(
            @Param("themeIds") List<Integer> themeIds,
            @Param("questionTypes") List<String> questionTypes
    );
}
