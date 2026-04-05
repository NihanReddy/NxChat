package com.chatflow.dto;

import java.time.LocalDateTime;

public class ChatSummaryDTO {
    private Long chatId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String recipientAvatarUrl;
    private String recipientStatus;
    private Boolean recipientDeleted;
    private String lastMessageContent;
    private LocalDateTime timestamp;
    private Long unreadCount;
    private Boolean isGroup;

    public ChatSummaryDTO() {
    }

    public ChatSummaryDTO(Long chatId, String recipientName, String lastMessageContent, LocalDateTime timestamp, Long unreadCount, Boolean isGroup) {
        this.chatId = chatId;
        this.recipientName = recipientName;
        this.lastMessageContent = lastMessageContent;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.isGroup = isGroup;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientAvatarUrl() {
        return recipientAvatarUrl;
    }

    public void setRecipientAvatarUrl(String recipientAvatarUrl) {
        this.recipientAvatarUrl = recipientAvatarUrl;
    }

    public String getRecipientStatus() {
        return recipientStatus;
    }

    public void setRecipientStatus(String recipientStatus) {
        this.recipientStatus = recipientStatus;
    }

    public Boolean getRecipientDeleted() {
        return recipientDeleted;
    }

    public void setRecipientDeleted(Boolean recipientDeleted) {
        this.recipientDeleted = recipientDeleted;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }
}
