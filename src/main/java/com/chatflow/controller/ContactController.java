package com.chatflow.controller;

import com.chatflow.dto.PendingRequestDTO;
import com.chatflow.model.Contact;
import com.chatflow.model.User;
import com.chatflow.service.ChatService;
import com.chatflow.service.ContactService;
import com.chatflow.service.GooglePeopleService;
import com.chatflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired private ContactService contactService;
    @Autowired private UserService userService;
    @Autowired private GooglePeopleService googlePeopleService;
    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // ──────────────────────────────────────────────────────────────
    //  GET: accepted contacts list
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<List<User>> getMyContacts(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        User user = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow();
        return ResponseEntity.ok(contactService.getMyContacts(user.getId()));
    }

    // ──────────────────────────────────────────────────────────────
    //  GET: pending incoming requests (both paths)
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/requests")
    public ResponseEntity<List<PendingRequestDTO>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        User user = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow();
        return ResponseEntity.ok(contactService.getPendingRequests(user.getId()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingRequestDTO>> getPending(@AuthenticationPrincipal UserDetails userDetails) {
        return getRequests(userDetails);
    }

    // ──────────────────────────────────────────────────────────────
    //  POST: send a connection request (or auto-accept reciprocal)
    // ──────────────────────────────────────────────────────────────
    @PostMapping({"/add", "/request"})
    public ResponseEntity<?> addContact(@RequestBody Map<String, String> payload,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        User me = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow();
        String identifier = payload.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("email");
        }
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("phone");
        }
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Target email or mobile number required"));
        }

        try {
            Contact contact = contactService.sendRequest(me.getId(), identifier);

            if ("ACCEPTED".equals(contact.getStatus())) {
                // ── Reciprocal auto-accept path ──────────────────
                // Re-fetch with eager data (service already handles this)
                var chat = chatService.getOrCreatePrivateChat(contact.getOwner(), contact.getContactUser());
                long chatId = chat.getId();

                // Build safe DTO payloads for WebSocket (no lazy proxies)
                Map<String, Object> forOwner = new HashMap<>();
                forOwner.put("type", "REQUEST_ACCEPTED_SELF");
                forOwner.put("chatId", chatId);
                forOwner.put("senderEmail", contact.getContactUser().getEmail());
                forOwner.put("senderUsername", contact.getContactUser().getUsername());

                Map<String, Object> forReceiver = new HashMap<>();
                forReceiver.put("type", "REQUEST_ACCEPTED");
                forReceiver.put("chatId", chatId);
                forReceiver.put("senderEmail", contact.getOwner().getEmail());
                forReceiver.put("senderUsername", contact.getOwner().getUsername());

                messagingTemplate.convertAndSendToUser(contact.getOwner().getEmail(), "/queue/contacts", forOwner);
                messagingTemplate.convertAndSendToUser(contact.getContactUser().getEmail(), "/queue/contacts", forReceiver);

                Map<String, Object> resp = new HashMap<>();
                resp.put("message", "Digital Handshake completed with " + identifier);
                resp.put("chatId", chatId);
                return ResponseEntity.ok(resp);
            }

            // ── Normal pending path ──────────────────────────────
            // Notify receiver via WebSocket with only safe scalar data
            Map<String, Object> wsPayload = new HashMap<>();
            wsPayload.put("type", "REQUEST_RECEIVED");
            wsPayload.put("requestId", contact.getId());
            wsPayload.put("senderUsername", contact.getOwner().getUsername());
            wsPayload.put("senderEmail", contact.getOwner().getEmail());
            wsPayload.put("senderId", contact.getOwner().getId());

            messagingTemplate.convertAndSendToUser(
                    contact.getContactUser().getEmail(),
                    "/queue/contacts",
                    wsPayload
            );

            return ResponseEntity.ok(Map.of("message", "Digital Handshake sent to " + identifier));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Handshake failed: " + e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  POST: accept or deny a request
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/respond")
    public ResponseEntity<?> respond(@RequestBody Map<String, Object> payload,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        User actingUser = userService.findActiveByIdentity(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Long requestId = Long.parseLong(payload.get("requestId").toString());
        String action = payload.get("action").toString();
        Contact contact = contactService.respondToRequest(requestId, action, actingUser.getId());

        if ("ACCEPT".equalsIgnoreCase(action)) {
            var chat = chatService.getOrCreatePrivateChat(contact.getOwner(), contact.getContactUser());
            long chatId = chat.getId();

            // Notify the original sender (owner) that their request was accepted
            Map<String, Object> forSender = new HashMap<>();
            forSender.put("type", "REQUEST_ACCEPTED");
            forSender.put("chatId", chatId);
            forSender.put("senderEmail", contact.getContactUser().getEmail());
            forSender.put("senderUsername", contact.getContactUser().getUsername());
            messagingTemplate.convertAndSendToUser(contact.getOwner().getEmail(), "/queue/contacts", forSender);

            // Notify the acceptor themselves
            Map<String, Object> forAcceptor = new HashMap<>();
            forAcceptor.put("type", "REQUEST_ACCEPTED_SELF");
            forAcceptor.put("chatId", chatId);
            forAcceptor.put("senderEmail", contact.getOwner().getEmail());
            forAcceptor.put("senderUsername", contact.getOwner().getUsername());
            messagingTemplate.convertAndSendToUser(contact.getContactUser().getEmail(), "/queue/contacts", forAcceptor);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Request Accepted");
            resp.put("chatId", chatId);
            return ResponseEntity.ok(resp);

        } else {
            if (contact.getOwner() != null) {
                messagingTemplate.convertAndSendToUser(
                        contact.getOwner().getEmail(),
                        "/queue/contacts",
                        Map.of("type", "REQUEST_DENIED", "requestId", requestId)
                );
            }
            return ResponseEntity.ok(Map.of("message", "Request Dismissed"));
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  POST: search a user by email
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/search")
    public ResponseEntity<?> searchUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        return userService.getUserByEmail(email)
                .map(u -> { u.setPassword(null); return ResponseEntity.ok(u); })
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    //  POST: sync Google contacts
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/sync-google")
    public ResponseEntity<?> syncGoogleContacts(@RequestBody Map<String, String> payload,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        User user = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow();
        String accessToken = payload.get("accessToken");

        try {
            List<String> emails = googlePeopleService.getContactEmails(accessToken, user.getEmail());
            int addedCount = 0;
            for (String email : emails) {
                try {
                    contactService.sendRequest(user.getId(), email);
                    addedCount++;
                } catch (Exception ignored) { }
            }
            return ResponseEntity.ok(Map.of("message", "Digital Handshake sent to " + addedCount + " synced contacts."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Google Sync failed: " + e.getMessage()));
        }
    }
}
