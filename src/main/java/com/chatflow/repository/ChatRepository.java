package com.chatflow.repository;

import com.chatflow.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT cm1.chat FROM ChatMember cm1 JOIN ChatMember cm2 ON cm1.chat.id = cm2.chat.id " +
           "WHERE cm1.user.id = :user1Id AND cm2.user.id = :user2Id AND cm1.chat.isGroup = false")
    Optional<Chat> findPrivateChatBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}
