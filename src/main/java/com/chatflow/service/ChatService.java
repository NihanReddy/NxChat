package com.chatflow.service; // trigger re-index

import com.chatflow.dto.ChatSummaryDTO;
import com.chatflow.dto.GroupMemberDTO;
import com.chatflow.dto.MessageDTO;
import com.chatflow.model.Chat;
import com.chatflow.model.ChatMember;
import com.chatflow.model.Message;
import com.chatflow.model.User;
import com.chatflow.repository.ChatMemberRepository;
import com.chatflow.repository.ChatRepository;
import com.chatflow.repository.MessageRepository;
import com.chatflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    public List<Chat> listChats() {
        return chatRepository.findAll();
    }

    @Transactional
    public Chat getOrCreatePrivateChat(User user1, User user2) {
        return chatRepository.findPrivateChatBetweenUsers(user1.getId(), user2.getId())
            .orElseGet(() -> {
                Chat newChat = new Chat(null, false);
                newChat = chatRepository.saveAndFlush(newChat);
                chatMemberRepository.save(new ChatMember(newChat, user1, "MEMBER"));
                chatMemberRepository.save(new ChatMember(newChat, user2, "MEMBER"));
                return newChat;
            });
    }

    @Transactional
    public Chat createGroup(String name, Long adminUserId, java.util.List<Long> memberIds) {
        User admin = userRepository.findById(adminUserId).orElseThrow(() -> new RuntimeException("Admin user not found"));
        Chat newChat = new Chat(name, true);
        final Chat savedChat = chatRepository.saveAndFlush(newChat);
        chatMemberRepository.save(new ChatMember(savedChat, admin, "ADMIN"));
        
        if (memberIds != null) {
            for (Long mid : memberIds) {
                if (mid.equals(adminUserId)) continue;
                userRepository.findById(mid).ifPresent(u -> {
                    chatMemberRepository.save(new ChatMember(savedChat, u, "MEMBER"));
                });
            }
        }
        return savedChat;
    }

    @Transactional
    public Chat createDM(Long userAId, Long userBId) {
        User a = userRepository.findById(userAId).orElseThrow();
        User b = userRepository.findById(userBId).orElseThrow();
        return getOrCreatePrivateChat(a, b);
    }

    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }

    public ChatMember addUserToGroup(Long chatId, Long userId, Long actingUserId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        if (!chat.isGroup()) throw new RuntimeException("Not a group");
        ensureGroupAdmin(chat, actingUserId);
        User user = userRepository.findById(userId).orElseThrow();
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new RuntimeException("Cannot add deleted account to group");
        }
        if (chatMemberRepository.findByChatAndUser(chat, user).isPresent()) {
            throw new RuntimeException("User is already a member of this group");
        }
        return chatMemberRepository.save(new ChatMember(chat, user, "MEMBER"));
    }

    public void removeUserFromGroup(Long chatId, Long userId, Long actingUserId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        if (!chat.isGroup()) throw new RuntimeException("Not a group");
        ensureGroupAdmin(chat, actingUserId);
        User user = userRepository.findById(userId).orElseThrow();
        ChatMember member = chatMemberRepository.findByChatAndUser(chat, user).orElseThrow();
        if ("ADMIN".equalsIgnoreCase(member.getMemberRole())) {
            throw new RuntimeException("Cannot remove group admins");
        }
        chatMemberRepository.delete(member);
    }

    public List<Message> getMessagesByChat(Long chatId, Long userId, int limit) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ensureChatMember(chat, user);
        if (limit <= 0 || limit > 100) limit = 50;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").ascending());
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable).getContent();
    }

    public List<MessageDTO> getMessageDTOsByChat(Long chatId, Long userId, int limit) {
        List<Message> messages = getMessagesByChat(chatId, userId, limit);
        return messages.stream().map(this::mapToMessageDTO).collect(java.util.stream.Collectors.toList());
    }

    public MessageDTO mapToMessageDTO(Message message) {
        MessageDTO.SenderDTO senderDTO = null;
        if (message.getSender() != null) {
            User sender = message.getSender();
            boolean senderDeleted = Boolean.TRUE.equals(sender.getDeleted());
            senderDTO = new MessageDTO.SenderDTO(
                    sender.getId(),
                senderDeleted ? UserService.DELETED_ACCOUNT_LABEL : sender.getUsername(),
                senderDeleted ? UserService.DELETED_ACCOUNT_LABEL : sender.getName(),
                senderDeleted ? null : sender.getEmail(),
                senderDeleted ? null : sender.getProfileImageUrl(),
                senderDeleted ? "DELETED" : (sender.getStatus() != null ? sender.getStatus() : "OFFLINE"),
                senderDeleted
            );
        }

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setStatus(message.getStatus() != null ? message.getStatus().name() : null);
        dto.setSender(senderDTO);

        return dto;
    }

    @Transactional
    public Message sendMessage(Long chatId, Message message, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        ensureChatMember(chat, user);
        message.setChat(chat);
        message.setSender(user);
        return messageRepository.save(message);
    }

    @Transactional
    public ChatMember updateLastReadMessage(Long chatId, String email, Long msgId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        User user = userRepository.findActiveByEmail(email)
                .or(() -> userRepository.findActiveByUsername(email))
                .or(() -> userRepository.findActiveByPhone(email))
                .orElseThrow(() -> new RuntimeException("User not found"));
        ChatMember member = ensureChatMember(chat, user);
        member.setLastReadMessageId(msgId);
        return chatMemberRepository.save(member);
    }

    public java.util.List<ChatSummaryDTO> getChatSummariesForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<ChatMember> members = chatMemberRepository.findByUser(user);
        System.out.println("[ChatService] Hub found " + members.size() + " connection points for user " + user.getEmail());
        
        return members.stream().map(ChatMember::getChat).map(chat -> {
            java.util.Optional<Message> lastMessage = messageRepository.findFirstByChatOrderByIdDesc(chat);
            String lastContent = lastMessage.map(Message::getContent).orElse("No dynamic signals yet");
            java.time.LocalDateTime lastTime = lastMessage.map(Message::getCreatedAt).orElse(null);
            long unreadCount = countUnreadMessages(chat.getId(), user.getEmail());

            ChatSummaryDTO dto = new ChatSummaryDTO(chat.getId(), chat.getName(), lastContent, lastTime, unreadCount, chat.isGroup());
            
            if (!chat.isGroup()) {
                User recipient = chatMemberRepository.findByChat(chat).stream()
                    .filter(m -> !m.getUser().getId().equals(userId))
                    .map(ChatMember::getUser)
                    .findFirst()
                    .orElse(user); // fallback to self if solo chat

                String rName = (recipient.getName() != null && !recipient.getName().isBlank()) ? recipient.getName() : recipient.getUsername();
                boolean recipientDeleted = Boolean.TRUE.equals(recipient.getDeleted());
                dto.setRecipientDeleted(recipientDeleted);
                if (recipientDeleted) {
                    dto.setRecipientName(UserService.DELETED_ACCOUNT_LABEL);
                    dto.setRecipientEmail(null);
                    dto.setRecipientPhone(null);
                    dto.setRecipientAvatarUrl(null);
                    dto.setRecipientStatus("DELETED");
                } else {
                    dto.setRecipientName(rName);
                    dto.setRecipientEmail(recipient.getEmail());
                    dto.setRecipientPhone(recipient.getPhone());
                    dto.setRecipientAvatarUrl(recipient.getProfileImageUrl());
                    dto.setRecipientStatus(recipient.getStatus());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChatMember> getChatMembers(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chatMemberRepository.findByChat(chat);
    }

    public List<GroupMemberDTO> getGroupMemberDTOs(Long chatId, Long requesterUserId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        if (!chat.isGroup()) {
            throw new RuntimeException("Not a group");
        }
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ensureChatMember(chat, requester);
        return chatMemberRepository.findByChat(chat)
                .stream()
                .map(GroupMemberDTO::new)
                .collect(Collectors.toList());
    }

    private ChatMember ensureChatMember(Chat chat, User user) {
        return chatMemberRepository.findByChatAndUser(chat, user)
                .orElseThrow(() -> new RuntimeException("You are not a member of this chat"));
    }

    private void ensureGroupAdmin(Chat chat, Long actingUserId) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new RuntimeException("Acting user not found"));
        ChatMember actingMember = chatMemberRepository.findByChatAndUser(chat, actingUser)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!"ADMIN".equalsIgnoreCase(actingMember.getMemberRole())) {
            throw new RuntimeException("Only group admins can manage members");
        }
    }

    @Transactional
    public Message saveMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        ensureChatMember(chat, sender);
        
        Message message = new Message(sender, chat, content);
        return messageRepository.save(message);
    }

    public long countUnreadMessages(Long chatId, String requesterEmail) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        User user = userRepository.findActiveByEmail(requesterEmail)
            .or(() -> userRepository.findActiveByUsername(requesterEmail))
            .or(() -> userRepository.findActiveByPhone(requesterEmail))
            .orElseThrow(() -> new RuntimeException("User not found"));
        ChatMember member = ensureChatMember(chat, user);

        Long lastRead = member.getLastReadMessageId();
        if (lastRead == null) {
            return messageRepository.countByChat(chat);
        }
        return messageRepository.countByChatAndIdGreaterThan(chat, lastRead);
    }

    @Transactional
    public void markAsRead(Long chatId, String email) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        User user = userRepository.findActiveByEmail(email)
            .or(() -> userRepository.findActiveByUsername(email))
            .or(() -> userRepository.findActiveByPhone(email))
            .orElseThrow(() -> new RuntimeException("User not found"));
        ChatMember member = ensureChatMember(chat, user);
        
        Optional<Message> lastMsg = messageRepository.findFirstByChatOrderByIdDesc(chat);
        lastMsg.ifPresent(message -> {
            member.setLastReadMessageId(message.getId());
            chatMemberRepository.save(member);
        });
    }

    @Transactional
    public void deleteChat(Long chatId, String requesterIdentity) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatMember requesterMembership = resolveRequesterMembership(chat, requesterIdentity)
            .orElseThrow(() -> new RuntimeException("You are not a member of this chat"));

        if (chat.isGroup() && !"ADMIN".equalsIgnoreCase(requesterMembership.getMemberRole())) {
            // Non-admins can only remove the group from their own account.
            chatMemberRepository.delete(requesterMembership);

            // If this was the last visible membership, clean up the orphaned group.
            if (chatMemberRepository.findByChat(chat).isEmpty()) {
                messageRepository.deleteByChat(chat);
                chatRepository.delete(chat);
            }
            return;
        }

        messageRepository.deleteByChat(chat);
        chatMemberRepository.deleteByChat(chat);
        chatRepository.delete(chat);
    }

    private Optional<ChatMember> resolveRequesterMembership(Chat chat, String requesterIdentity) {
        if (requesterIdentity == null || requesterIdentity.isBlank()) {
            return Optional.empty();
        }

        String normalizedIdentity = requesterIdentity.trim();

        Optional<User> resolvedUser = userRepository.findActiveByEmail(normalizedIdentity)
                .or(() -> userRepository.findActiveByUsername(normalizedIdentity))
                .or(() -> userRepository.findActiveByPhone(normalizedIdentity));

        if (resolvedUser.isPresent()) {
            Optional<ChatMember> membership = chatMemberRepository.findByChatAndUser(chat, resolvedUser.get());
            if (membership.isPresent()) {
                return membership;
            }
        }

        // Fallback: match directly against member identity values to handle legacy duplicate records.
        return chatMemberRepository.findByChat(chat)
                .stream()
                .filter(member -> {
                    User user = member.getUser();
                    if (user == null) return false;
                    String email = user.getEmail();
                    String username = user.getUsername();
                    String phone = user.getPhone();
                    return (email != null && email.equalsIgnoreCase(normalizedIdentity))
                            || (username != null && username.equalsIgnoreCase(normalizedIdentity))
                            || (phone != null && phone.equals(normalizedIdentity));
                })
                .findFirst();
    }
}
