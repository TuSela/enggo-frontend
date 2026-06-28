package com.nhom12.enggo_backend.repository;

import com.nhom12.enggo_backend.entity.identity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u ORDER BY COALESCE(u.elo, 0) DESC")
    Page<User> getLeaderBoardData(Pageable pageable);
    @Query("SELECT u FROM User u")
    Page<User> getAllUsers(Pageable pageable);

    // Find top 4 users ordered by elo descending
    List<User> findTop4ByOrderByEloDesc();
    boolean existsByUsername(String username);

    List<User> findAlLByOrderByEloDesc();

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    int countByEloGreaterThan(int elo);
}