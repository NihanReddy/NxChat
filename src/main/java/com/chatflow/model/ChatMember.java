package com.chatflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_member")
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id") // standardizing name
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String memberRole = "MEMBER";

    private Long lastReadMessageId;

    public ChatMember() {
    }

    public ChatMember(Chat chat, User user, String memberRole) {
        this.chat = chat;
        this.user = user;
        this.memberRole = memberRole;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}