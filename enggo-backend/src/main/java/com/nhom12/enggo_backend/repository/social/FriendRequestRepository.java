package com.nhom12.enggo_backend.repository.social;

import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.entity.social.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    boolean existsBySenderAndReceiver(User sender, User receiver);
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    List<FriendRequest> findAllByReceiver(User receiver);

    @Query("SELECT fr.receiver.id FROM FriendRequest fr WHERE fr.sender = :sender")
    List<Integer> findReceiverIdsBySender(@Param("sender") User sender);
}