package com.chatflow.repository;

import com.chatflow.model.Chat;
import com.chatflow.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    long countByChatAndIdGreaterThan(Chat chat, Long lastReadMessageId);
    long countByChat(Chat chat);
    Optional<Message> findFirstByChatOrderByIdDesc(Chat chat);
    
    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);
    Page<Message> findByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);
    void deleteByChat(Chat chat);
}
