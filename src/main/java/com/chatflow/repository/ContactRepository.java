package com.chatflow.repository;

import com.chatflow.model.Contact;
import com.chatflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOwnerId(Long ownerId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT c FROM Contact c JOIN FETCH c.owner JOIN FETCH c.contactUser " +
        "WHERE c.contactUser.id = :contactUserId AND c.status = :status"
    )
    List<Contact> findByContactUserIdAndStatus(
        @org.springframework.data.repository.query.Param("contactUserId") Long contactUserId,
        @org.springframework.data.repository.query.Param("status") String status
    );

    Optional<Contact> findByOwnerIdAndContactUserId(Long ownerId, Long contactUserId);
    void deleteByOwnerIdAndContactUserId(Long ownerId, Long contactUserId);
    void deleteByOwnerIdOrContactUserId(Long ownerId, Long contactUserId);
}
