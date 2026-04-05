package com.chatflow.dto;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String status;
    private SenderDTO sender;

    public MessageDTO() {}

    public MessageDTO(Long id, String content, LocalDateTime createdAt, String status, SenderDTO sender) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.status = status;
        this.sender = sender;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public SenderDTO getSender() { return sender; }
    public void setSender(SenderDTO sender) { this.sender = sender; }

    public static class SenderDTO {
        private Long id;
        private String username;
        private String name;
        private String email;
        private String profileImageUrl;
        private String status;
        private Boolean deleted;

        public SenderDTO() {}

        public SenderDTO(Long id, String username, String name, String email, String profileImageUrl, String status, Boolean deleted) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.email = email;
            this.profileImageUrl = profileImageUrl;
            this.status = status;
            this.deleted = deleted;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Boolean getDeleted() { return deleted; }
        public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    }
}
