package com.chatflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner; // The person adding the contact

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private User contactUser; // The person being added

    private String status; // PENDING, ACCEPTED, BLOCKED
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public Contact() {
    }

    public Contact(User owner, User contactUser, String status) {
        this.owner = owner;
        this.contactUser = contactUser;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getContactUser() {
        return contactUser;
    }

    public void setContactUser(User contactUser) {
        this.contactUser = contactUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder manual implementation for ContactService compatibility
    public static ContactBuilder builder() {
        return new ContactBuilder();
    }

    public static class ContactBuilder {
        private User owner;
        private User contactUser;
        private String status;

        public ContactBuilder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public ContactBuilder contactUser(User contactUser) {
            this.contactUser = contactUser;
            return this;
        }

        public ContactBuilder status(String status) {
            this.status = status;
            return this;
        }

        public Contact build() {
            return new Contact(owner, contactUser, status);
        }
    }
}
