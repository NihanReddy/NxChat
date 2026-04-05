package com.chatflow.repository;

import com.chatflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM user u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) AND (u.deleted = false OR u.deleted IS NULL) ORDER BY u.id DESC LIMIT 1", nativeQuery = true)
    Optional<User> findActiveByEmail(@Param("email") String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Boolean existsByUsername(String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND (u.deleted = false OR u.deleted IS NULL)")
    Boolean existsActiveByUsername(@Param("username") String username);

    Boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) AND (u.deleted = false OR u.deleted IS NULL)")
    Boolean existsActiveByEmail(@Param("email") String email);

    Optional<User> findByPhone(String phone);
    @Query(value = "SELECT * FROM user u WHERE u.phone = :phone AND (u.deleted = false OR u.deleted IS NULL) ORDER BY u.id DESC LIMIT 1", nativeQuery = true)
    Optional<User> findActiveByPhone(@Param("phone") String phone);
    Optional<User> findByPhoneAndDeletedFalse(String phone);
    Boolean existsByPhone(String phone);
    Optional<User> findByUsername(String username);
    @Query(value = "SELECT * FROM user u WHERE u.username = :username AND (u.deleted = false OR u.deleted IS NULL) ORDER BY u.id DESC LIMIT 1", nativeQuery = true)
    Optional<User> findActiveByUsername(@Param("username") String username);
    Optional<User> findByUsernameAndDeletedFalse(String username);

    Boolean existsByUsernameAndDeletedFalse(String username);

    Boolean existsByEmailAndDeletedFalse(String email);

    java.util.List<User> findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(String emailFragment, String usernameFragment);

    @Query("SELECT u FROM User u WHERE (LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))) AND (u.deleted = false OR u.deleted IS NULL)")
    java.util.List<User> searchActiveUsers(@Param("query") String query);

    java.util.List<User> findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCaseAndDeletedFalse(String emailFragment, String usernameFragment);
}
