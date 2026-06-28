package com.nhom12.enggo_backend.repository.social;

import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    // Tìm cuộc trò chuyện PRIVATE giữa 2 user (tránh tạo trùng)
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.type = 'PRIVATE'
        AND EXISTS (SELECT p FROM Participant p WHERE p.conversation = c AND p.user = :u1)
        AND EXISTS (SELECT p FROM Participant p WHERE p.conversation = c AND p.user = :u2)
    """)
    Optional<Conversation> findPrivateConversation(@Param("u1") User u1, @Param("u2") User u2);
}