package com.nhom12.enggo_backend.repository.social;

import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.Conversation;
import com.nhom12.enggo_backend.entity.social.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    List<Participant> findByUser(User user);

    List<Participant> findByConversation(Conversation conversation);

    Optional<Participant> findByConversationAndUser(Conversation conversation, User user);

    boolean existsByConversationAndUser(Conversation conversation, User user);
}