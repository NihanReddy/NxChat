package com.chatflow.controller;

import com.chatflow.dto.ChatSummaryDTO;
import com.chatflow.dto.GroupMemberDTO;
import com.chatflow.dto.MessageDTO;
import com.chatflow.model.Chat;
import com.chatflow.model.ChatMember;
import com.chatflow.model.Message;
import com.chatflow.model.User;
import com.chatflow.service.ChatService;
import com.chatflow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<Chat>> listChats() {
        return ResponseEntity.ok(chatService.listChats());
    }

    @GetMapping("/my-chats")
    public ResponseEntity<?> listMyChats(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(chatService.getChatSummariesForUser(user.getId()));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> recentChats(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userService.findByEmail(userDetails.getUsername())
                .or(() -> userService.findByUsername(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));
        java.util.List<ChatSummaryDTO> summaries = chatService.getChatSummariesForUser(user.getId());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/private")
    public ResponseEntity<?> getOrCreatePrivateChat(@RequestParam("userId") Long targetUserId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User source = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User target = userService.getById(targetUserId).orElseThrow(() -> new RuntimeException("Target user not found"));
        Chat chat = chatService.getOrCreatePrivateChat(source, target);
        return ResponseEntity.ok(java.util.Map.of("chatId", chat.getId(), "chat", chat));
    }

    @PostMapping("/group")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> payload,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String name = (String) payload.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Group name is required");
        }
        
        java.util.List<?> memberIdsRaw = (java.util.List<?>) payload.get("memberIds");
        java.util.List<Long> memberIds = null;
        if (memberIdsRaw != null) {
            memberIds = memberIdsRaw.stream()
                .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                .collect(java.util.stream.Collectors.toList());
        }

        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Chat chat = chatService.createGroup(name, user.getId(), memberIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    @PostMapping("/dm")
    public ResponseEntity<?> createDm(@RequestBody Map<String, Long> payload,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Long targetUserId = payload.get("userId");
        if (targetUserId == null) {
            return ResponseEntity.badRequest().body("Target userId is required");
        }
        User source = userService.findByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Chat chat = chatService.createDM(source.getId(), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> members(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User acting = userService.findActiveByIdentity(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Chat chat = chatService.getChatById(id).orElse(null);
        if (chat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat not found");
        }
        if (!chat.isGroup()) {
            return ResponseEntity.badRequest().body("This endpoint is only for group chats");
        }
        List<GroupMemberDTO> members = chatService.getGroupMemberDTOs(id, acting.getId());
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Long> payload,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        Long userIdToAdd = payload.get("userId");
        if (userIdToAdd == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        User acting = userService.findActiveByIdentity(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            ChatMember member = chatService.addUserToGroup(id, userIdToAdd, acting.getId());
            return ResponseEntity.ok(member);
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "Unable to add member" : e.getMessage();
            if (message.contains("Only group admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            }
            if (message.contains("already a member") || message.contains("Cannot add deleted")) {
                return ResponseEntity.badRequest().body(message);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User acting = userService.findActiveByIdentity(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            chatService.removeUserFromGroup(id, userId, acting.getId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "Unable to remove member" : e.getMessage();
            if (message.contains("Only group admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            }
            if (message.contains("Cannot remove group admins")) {
                return ResponseEntity.badRequest().body(message);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id, 
            @RequestParam(required = false, defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userService.findActiveByIdentity(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(chatService.getMessageDTOsByChat(id, user.getId(), limit));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestBody Message message,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (!isValidMessage(message)) {
            return ResponseEntity.badRequest().body("Message content must be non-empty");
        }
        User user = userService.findActiveByIdentity(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        Message saved = chatService.sendMessage(id, message, user.getId());
        MessageDTO dto = chatService.mapToMessageDTO(saved);
        messagingTemplate.convertAndSend("/topic/chat/" + id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @MessageMapping("/chat/{chatId}/typing")
    public void websocketTyping(@DestinationVariable Long chatId, @Payload Map<String, Object> payload, java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        String username = payload.getOrDefault("username", principal.getName()).toString();
        Object senderIdObj = payload.get("senderId");
        Long senderId = senderIdObj instanceof Number ? ((Number) senderIdObj).longValue() : null;
        Map<String, Object> event = new HashMap<>();
        event.put("type", "TYPING");
        event.put("username", username);
        event.put("senderId", senderId);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, event);
    }

    @MessageMapping("/chat.send")
    public void websocketSend(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        Object chatIdObj = payload.get("chatId");
        if (!(chatIdObj instanceof Number)) {
            throw new RuntimeException("chatId is required");
        }

        Long chatId = ((Number) chatIdObj).longValue();
        String content = (String) payload.get("content");
        if (content == null || content.isBlank()) {
            return;
        }

        User user = userService.findByEmail(principal.getName())
                .or(() -> userService.findByUsername(principal.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setContent(content);
        Message saved = chatService.sendMessage(chatId, message, user.getId());
        MessageDTO dto = chatService.mapToMessageDTO(saved);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);
    }

    @MessageMapping("/chat/{chatId}/readReceipt")
    public void websocketReadReceipt(@DestinationVariable Long chatId, @Payload Map<String, Object> payload, java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        Object messageIdObj = payload.get("messageId");
        if (messageIdObj == null) return;
        Long messageId = messageIdObj instanceof Number ? ((Number) messageIdObj).longValue() : null;
        if (messageId == null) return;

        // Persist last read position and optionally compute unread count
        chatService.updateLastReadMessage(chatId, principal.getName(), messageId);

        String username = payload.getOrDefault("username", principal.getName()).toString();

        Map<String, Object> event = new HashMap<>();
        event.put("type", "READ_RECEIPT");
        event.put("username", username);
        event.put("messageId", messageId);
        event.put("chatId", chatId);

        long unreadCount = chatService.countUnreadMessages(chatId, principal.getName());
        event.put("unreadCount", unreadCount);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, event);
    }

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void websocketSendMessage(@DestinationVariable Long chatId, @Payload Message chatMessage, java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        User user = userService.findByEmail(principal.getName())
                .or(() -> userService.findByUsername(principal.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message saved = chatService.sendMessage(chatId, chatMessage, user.getId());
        MessageDTO dto = chatService.mapToMessageDTO(saved);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);
    }

    private boolean isValidMessage(Message message) {
        return message != null && message.getContent() != null && !message.getContent().isBlank();
    }

    @MessageMapping("/chat/{chatId}/addUser")
    public void websocketAddUser(@DestinationVariable Long chatId, @Payload Map<String, String> event, java.security.Principal principal) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "JOIN");
        result.put("username", principal != null ? principal.getName() : event.get("username"));
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markChatAsRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        chatService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            chatService.deleteChat(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "Unable to delete chat" : e.getMessage();
            if (message.contains("Only group admins")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            }
            if (message.contains("not a member")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }
}
