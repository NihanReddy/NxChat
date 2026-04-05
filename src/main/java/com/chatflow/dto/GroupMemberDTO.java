package com.chatflow.dto;

import com.chatflow.model.ChatMember;
import com.chatflow.model.User;
import com.chatflow.service.UserService;

public class GroupMemberDTO {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String profileImageUrl;
    private String status;
    private String memberRole;
    private Boolean deleted;

    public GroupMemberDTO() {
    }

    public GroupMemberDTO(ChatMember member) {
        User user = member.getUser();
        boolean isDeleted = user != null && Boolean.TRUE.equals(user.getDeleted());

        this.userId = user != null ? user.getId() : null;
        this.username = isDeleted ? UserService.DELETED_ACCOUNT_LABEL : (user != null ? user.getUsername() : null);
        this.name = isDeleted ? UserService.DELETED_ACCOUNT_LABEL : (user != null ? user.getName() : null);
        this.email = isDeleted ? null : (user != null ? user.getEmail() : null);
        this.profileImageUrl = isDeleted ? null : (user != null ? user.getProfileImageUrl() : null);
        this.status = isDeleted ? "DELETED" : (user != null ? user.getStatus() : "OFFLINE");
        this.memberRole = member.getMemberRole();
        this.deleted = isDeleted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
