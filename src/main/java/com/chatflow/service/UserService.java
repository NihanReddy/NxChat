package com.chatflow.service;

import com.chatflow.model.User;
import com.chatflow.repository.ContactRepository;
import com.chatflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    public static final String DELETED_ACCOUNT_LABEL = "Deleted Account";

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    public User registerUser(User user) {
        if (userRepository.existsActiveByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }
        if (userRepository.existsActiveByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole("ADMIN");
        userRepository.save(user);
    }

    public boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @Autowired
    private OtpService otpService;

    public String generateOTP(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return otpService.generateOtp(email);
        }
        throw new RuntimeException("User not found");
    }

    public OtpService.OtpVerificationResult verifyOTP(String email, String enteredOtp) {
        return otpService.verifyOtp(email, enteredOtp);
    }

    public void clearOTP(String email) {
        otpService.clearOtp(email);
    }

    public Optional<User> login(String identifier, String rawPassword) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalizedIdentifier = identifier.trim();

        Optional<User> userOpt = userRepository.findActiveByEmail(normalizedIdentifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findActiveByUsername(normalizedIdentifier);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findActiveByPhone(normalizedIdentifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            boolean match;
            try {
                match = encoder.matches(rawPassword, user.getPassword());
            } catch (IllegalArgumentException ex) {
                // Legacy rows may contain non-bcrypt passwords.
                match = rawPassword.equals(user.getPassword());
            }

            if (!match && rawPassword.equals(user.getPassword())) {
                match = true;
            }

            if (match) {
                if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
                    user.setPassword(encoder.encode(rawPassword));
                    userRepository.save(user);
                }
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsActiveByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findActiveByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findActiveByUsername(username);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findActiveByPhone(phone);
    }

    public Optional<User> findActiveByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            return Optional.empty();
        }
        String normalizedIdentity = identity.trim();
        return userRepository.findActiveByEmail(normalizedIdentity)
                .or(() -> userRepository.findActiveByUsername(normalizedIdentity))
                .or(() -> userRepository.findActiveByPhone(normalizedIdentity));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsActiveByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getDeleted())).toList();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User payload) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Identity not found."));
        if (payload.getUsername() != null) {
            String requestedUsername = payload.getUsername().trim();
            if (!requestedUsername.isBlank() && !requestedUsername.equals(existing.getUsername())) {
                Optional<User> usernameOwner = userRepository.findActiveByUsername(requestedUsername);
                if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(existing.getId())) {
                    throw new RuntimeException("Username already exists!");
                }
                existing.setUsername(requestedUsername);
            }
        }
        if (payload.getPassword() != null && !payload.getPassword().isEmpty()) {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            existing.setPassword(encoder.encode(payload.getPassword()));
        }
        if (payload.getPhone() != null) existing.setPhone(payload.getPhone());
        return userRepository.save(existing);
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findActiveByEmail(email).orElseThrow(() -> new RuntimeException("Identity not found in system grid."));
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        existing.setDeleted(true);
        existing.setStatus("OFFLINE");
        existing.setName(DELETED_ACCOUNT_LABEL);
        existing.setUsername("deleted_user_" + existing.getId());
        existing.setProfileImageUrl(null);
        userRepository.save(existing);
        contactRepository.deleteByOwnerIdOrContactUserId(existing.getId(), existing.getId());
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findActiveByEmail(email);
    }

    public java.util.List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return java.util.Collections.emptyList();
        }
        String normalized = query.trim();
        return userRepository.searchActiveUsers(normalized);
    }

    public void deleteCurrentUserAccount(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("User identity is missing");
        }

        User existing = findActiveByIdentity(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        deleteUser(existing.getId());
    }
}

