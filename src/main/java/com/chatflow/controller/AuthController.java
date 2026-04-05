package com.chatflow.controller;

import com.chatflow.model.User;
import com.chatflow.service.EmailService;
import com.chatflow.service.OtpService;
import com.chatflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.chatflow.security.CustomUserDetailsService customUserDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already in system grid."));
        }
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username identity already claimed."));
        }
        // Never trust privilege fields from the client payload.
        request.setRole("USER");
        request.setDeleted(false);
        request.setPassword(customUserDetailsService.encodePassword(request.getPassword()));
        User saved = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Atmospheric Access Granted.", "user", saved));
    }

    @Autowired
    private com.chatflow.security.JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");
        if (email != null) {
            email = email.trim();
        }
        String password = payload.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Credentials missing. Atmospheric Precision requires complete identifiers."));
        }

        Optional<User> userOpt = userService.login(email, password);
        if (userOpt.isPresent()) {
    User user = userOpt.get();
    String token = jwtUtil.generateToken(
            org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build());
    Map<String, Object> data = new HashMap<>();
    data.put("user", user);
    data.put("token", token);
    data.put("message", "Login successful");
    return ResponseEntity.ok(data);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Authentication Failure. Digital signature mismatch."));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String clientIp = extractClientIp(request);
        String otp;
        try {
            if (!otpService.canRequestOtp(email, clientIp)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("OTP request limit exceeded for this email or IP. Please try again later.");
            }
            otp = otpService.generateOtp(email);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("OTP service unavailable. Please try again shortly.");
        }

        boolean sent = emailService.sendOtpEmail(email, otp);
        if (!sent) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Unable to send OTP email right now. Please try again.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("message", "OTP sent to your email");
        return ResponseEntity.ok(data);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        OtpService.OtpVerificationResult result;
        try {
            result = otpService.verifyOtp(email, otp);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "OTP service unavailable. Please try again shortly."));
        }
        if (result.isTooManyAttempts()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Lockout Phase Active. " + result.getMessage()));
        }
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Hash Mismatch. " + result.getMessage()));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build());
        otpService.clearOtp(email);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("token", token);
        data.put("message", "OTP login successful");
        return ResponseEntity.ok(data);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Incomplete transmission. All fields required.");
        }

        OtpService.OtpVerificationResult result;
        try {
            result = otpService.verifyOtp(email, otp);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "OTP service unavailable. Please try again shortly."));
        }
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Validation failed: " + result.getMessage()));
        }

        try {
            userService.updatePasswordByEmail(email, newPassword);
            otpService.clearOtp(email);
            return ResponseEntity.ok(Map.of("message", "Credential reset successful. Grid access restored."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to commit identity change.");
        }
    }

    @PostMapping("/promote/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> promote(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok("User promoted to ADMIN");
    }
}
