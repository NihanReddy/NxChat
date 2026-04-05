package com.chatflow.controller;

import com.chatflow.model.User;
import com.chatflow.security.CustomUserDetailsService;
import com.chatflow.security.JwtUtil;
import com.chatflow.service.EmailService;
import com.chatflow.service.OtpService;
import com.chatflow.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$abcdefghijklmnopqrstuv");
    }

    @Test
    void registerShouldReturnCreatedWhenUserNew() throws Exception {
        when(userService.existsByEmail("test@example.com")).thenReturn(false);
        when(userService.existsByUsername("testuser")).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void registerShouldReturnConflictWhenExistingEmail() throws Exception {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void loginShouldReturnUnauthorizedWhenInvalidCredentials() throws Exception {
        when(userService.login("test@example.com", "wrongpass")).thenReturn(Optional.empty());

        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        payload.put("password", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsValid() throws Exception {

        when(userService.login("test@example.com", "correctpass")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

        Map<String, String> payload = new HashMap<>();

        payload.put("email", "test@example.com");
        payload.put("password", "correctpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }
            @Test
            void sendOtpShouldReturnOkWhenOtpGeneratedAndEmailSent() throws Exception {
            when(userService.existsByEmail("test@example.com")).thenReturn(true);
            when(otpService.canRequestOtp(eq("test@example.com"), any(String.class))).thenReturn(true);
            when(otpService.generateOtp("test@example.com")).thenReturn("654321");
            when(emailService.sendOtpEmail(eq("test@example.com"), eq("654321"))).thenReturn(true);

            Map<String, String> payload = Map.of("email", "test@example.com");

            mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));
            }

            @Test
            void sendOtpShouldReturnServiceUnavailableWhenEmailFails() throws Exception {
            when(userService.existsByEmail("test@example.com")).thenReturn(true);
            when(otpService.canRequestOtp(eq("test@example.com"), any(String.class))).thenReturn(true);
            when(otpService.generateOtp("test@example.com")).thenReturn("654321");
            when(emailService.sendOtpEmail(eq("test@example.com"), eq("654321"))).thenReturn(false);

            Map<String, String> payload = Map.of("email", "test@example.com");

            mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isServiceUnavailable());
            }

    @Test
    void sendOtpShouldReturnNotFoundWhenUnknownEmail() throws Exception {
        when(userService.existsByEmail("missing@example.com")).thenReturn(false);

        Map<String, String> payload = Map.of("email", "missing@example.com");

        mockMvc.perform(post("/api/auth/send-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isNotFound());
    }
}
