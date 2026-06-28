package com.nhom12.enggo_backend.repository.social;

import com.nhom12.enggo_backend.entity.social.Conversation;
import com.nhom12.enggo_backend.entity.social.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);
}