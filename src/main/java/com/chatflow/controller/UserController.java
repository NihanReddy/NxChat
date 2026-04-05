package com.chatflow.controller;

import com.chatflow.model.User;
import com.chatflow.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    private boolean isAuthorizedForUserId(Long targetUserId, UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }

        Optional<User> requesterOpt = userService.findActiveByIdentity(userDetails.getUsername());
        if (requesterOpt.isEmpty()) {
            return false;
        }

        User requester = requesterOpt.get();
        if ("ADMIN".equalsIgnoreCase(requester.getRole())) {
            return true;
        }

        return requester.getId().equals(targetUserId);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return ResponseEntity.status(401).build();
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
    user.setPassword(null);
    return ResponseEntity.ok(user);
}

        @DeleteMapping("/me")
        public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            try {
                userService.deleteCurrentUserAccount(userDetails.getUsername());
                return ResponseEntity.noContent().build();
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete account right now");
            }
        }

    @GetMapping("/{id}")
public ResponseEntity<?> getUserById(@PathVariable Long id) {
    Optional<User> user = userService.getById(id);

    if (user.isPresent()) {
        return ResponseEntity.ok(user.get());
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
}

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("query is required");
        }
        return ResponseEntity.ok(userService.searchUsers(query));
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User request, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (!isAuthorizedForUserId(id, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        try {
            User updated = userService.updateUser(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (!isAuthorizedForUserId(id, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
