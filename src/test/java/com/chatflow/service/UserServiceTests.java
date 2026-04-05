package com.chatflow.service;

import com.chatflow.model.User;
import com.chatflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword(new BCryptPasswordEncoder().encode("secret"));
    }

    @Test
    void registerUserShouldThrowWhenEmailExists() {
        when(userRepository.existsActiveByEmail(user.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("Email already exists!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldThrowWhenUsernameExists() {
        when(userRepository.existsActiveByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.existsActiveByUsername(user.getUsername())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(user));
        assertEquals("Username already exists!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginShouldReturnUserWhenPasswordMatches() {
        when(userRepository.findActiveByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.login(user.getEmail(), "secret");

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    void loginShouldReturnEmptyWhenPasswordDoesNotMatch() {
        when(userRepository.findActiveByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.login(user.getEmail(), "wrong");

        assertTrue(result.isEmpty());
    }
}
