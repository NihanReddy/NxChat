package com.chatflow.dto;

import com.chatflow.model.Contact;

/**
 * DTO for pending connection requests.
 * Carries all data needed by the frontend without lazy-loading issues.
 */
public class PendingRequestDTO {
    private Long id;
    private SenderInfo owner;
    private String status;

    public PendingRequestDTO() {}

    public PendingRequestDTO(Contact contact) {
        this.id = contact.getId();
        this.status = contact.getStatus();
        if (contact.getOwner() != null) {
            this.owner = new SenderInfo(
                contact.getOwner().getId(),
                contact.getOwner().getUsername(),
                contact.getOwner().getEmail(),
                contact.getOwner().getName()
            );
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SenderInfo getOwner() { return owner; }
    public void setOwner(SenderInfo owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static class SenderInfo {
        private Long id;
        private String username;
        private String email;
        private String name;

        public SenderInfo() {}

        public SenderInfo(Long id, String username, String email, String name) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
