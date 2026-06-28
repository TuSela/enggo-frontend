package com.nhom12.enggo_backend.repository.social;

import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Integer> {

    @Query("SELECT f FROM Friend f WHERE f.user1 = :user OR f.user2 = :user")
    List<Friend> findAllByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    boolean existsByUsers(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT f FROM Friend f WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    Optional<Friend> findByUsers(@Param("u1") User u1, @Param("u2") User u2);

    // THÊM MỚI: Lấy bạn bè đang online
    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1 = :user OR f.user2 = :user)
        AND (
            (f.user1 = :user AND f.user2.status = 'ONLINE') OR
            (f.user2 = :user AND f.user1.status = 'ONLINE')
        )
    """)
    List<Friend> findOnlineFriends(@Param("user") User user);

    // THÊM MỚI: Tìm kiếm bạn bè theo username
    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user1 = :user OR f.user2 = :user)
        AND (
            LOWER(f.user1.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(f.user2.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    List<Friend> searchFriendsByUsername(@Param("user") User user, @Param("keyword") String keyword);
}