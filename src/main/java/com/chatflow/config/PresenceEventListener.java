package com.chatflow.config;

import com.chatflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {

    @Autowired
    private UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (event.getUser() != null) {
            String email = event.getUser().getName(); // Assuming Principal is created with name
            updateUserStatus(email, "ONLINE");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            String email = event.getUser().getName();
            updateUserStatus(email, "OFFLINE");
        }
    }

    private void updateUserStatus(String identifier, String newStatus) {
        userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByUsername(identifier))
            .ifPresent(user -> {
                user.setStatus(newStatus);
                userRepository.save(user);
            });
    }
}
