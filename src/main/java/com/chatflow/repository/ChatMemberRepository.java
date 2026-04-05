package com.chatflow.repository;

import com.chatflow.model.ChatMember;
import com.chatflow.model.Chat;
import com.chatflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    List<ChatMember> findByChat(Chat chat);
    Optional<ChatMember> findByChatAndUser(Chat chat, User user);
    List<ChatMember> findByUser(User user);
    void deleteByChat(Chat chat);
}