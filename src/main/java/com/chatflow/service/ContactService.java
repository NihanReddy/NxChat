package com.chatflow.service;

import com.chatflow.dto.PendingRequestDTO;
import com.chatflow.model.Contact;
import com.chatflow.model.User;
import com.chatflow.repository.ContactRepository;
import com.chatflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    public List<User> getMyContacts(Long userId) {
        return contactRepository.findByOwnerId(userId).stream()
                .filter(c -> "ACCEPTED".equals(c.getStatus()))
                .map(Contact::getContactUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PendingRequestDTO> getPendingRequests(Long userId) {
        return contactRepository.findByContactUserIdAndStatus(userId, "PENDING")
                .stream()
                .map(PendingRequestDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Contact sendRequest(Long ownerId, String identifier) {
        User owner = userRepository.findById(ownerId).orElseThrow();
        User contactUser = resolveUserByIdentifier(identifier)
            .orElseThrow(() -> new RuntimeException("Digital signature not found in the atmosphere."));

        if (owner.getId().equals(contactUser.getId())) {
            throw new RuntimeException("You cannot link with your own essence.");
        }

        // 1. Check if we already sent a request TO them
        Optional<Contact> outgoing = contactRepository.findByOwnerIdAndContactUserId(ownerId, contactUser.getId());
        if (outgoing.isPresent()) {
            Contact ex = outgoing.get();
            if ("PENDING".equals(ex.getStatus())) {
                throw new RuntimeException("Connection request already pending in the grid.");
            } else if ("ACCEPTED".equals(ex.getStatus())) {
                return ex;
            } else if ("DENIED".equals(ex.getStatus())) {
                ex.setStatus("PENDING");
                return contactRepository.save(ex);
            }
        }

        // 2. Check if they already sent a request TO us. If so, auto-accept it!
        Optional<Contact> incoming = contactRepository.findByOwnerIdAndContactUserId(contactUser.getId(), ownerId);
        if (incoming.isPresent()) {
            Contact in = incoming.get();
            if ("PENDING".equals(in.getStatus())) {
                // Someone wanted to connect with us, and we just tried to connect with them.
                // Let's just complete the handshake.
                return respondToRequest(in.getId(), "ACCEPT", ownerId);
            }
        }

        Contact contact = Contact.builder()
                .owner(owner)
                .contactUser(contactUser)
                .status("PENDING")
                .build();

        return contactRepository.save(contact);
    }

    private Optional<User> resolveUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalized = identifier.trim();
        Optional<User> userOpt = userRepository.findActiveByEmail(normalized)
                .or(() -> userRepository.findActiveByUsername(normalized));
        if (userOpt.isPresent()) {
            return userOpt;
        }

        Set<String> phoneCandidates = new LinkedHashSet<>();
        phoneCandidates.add(normalized);
        phoneCandidates.add(normalized.replaceAll("[\\s()\\-]", ""));

        for (String candidate : phoneCandidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            Optional<User> phoneUser = userRepository.findActiveByPhone(candidate);
            if (phoneUser.isPresent()) {
                return phoneUser;
            }
        }

        return Optional.empty();
    }

    @Transactional
    public Contact respondToRequest(Long requestId, String action, Long actingUserId) {
        Contact request = contactRepository.findById(requestId).orElseThrow();
        if (request.getContactUser() == null || !request.getContactUser().getId().equals(actingUserId)) {
            throw new RuntimeException("You are not allowed to respond to this request");
        }
        if ("ACCEPT".equalsIgnoreCase(action)) {
            request.setStatus("ACCEPTED");
            contactRepository.save(request);

            Contact reciprocal = Contact.builder()
                    .owner(request.getContactUser())
                    .contactUser(request.getOwner())
                    .status("ACCEPTED")
                    .build();
            contactRepository.save(reciprocal);

            // Ensure a one-to-one chat exists for the accepted contact
            chatService.getOrCreatePrivateChat(request.getOwner(), request.getContactUser());
            return request;
        } else {
            contactRepository.delete(request);
            return request;
        }
    }
}
