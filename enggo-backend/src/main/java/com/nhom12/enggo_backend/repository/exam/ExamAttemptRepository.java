package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.ExamAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Integer> {
    boolean existsByUserIdAndCompleteFalse (Integer userId);

    @Query("SELECT a.id FROM ExamAttempt a WHERE a.user.id = :userId")
    Page<Integer> findAllIds (Pageable pageable,@Param("userId") Integer userId);

    @Query("""
            SELECT a FROM ExamAttempt a
            WHERE a.id IN :ids
            ORDER BY a.completedAt
            """)
    List<ExamAttempt> findByIds (@Param("ids") List<Integer> ids);
}
