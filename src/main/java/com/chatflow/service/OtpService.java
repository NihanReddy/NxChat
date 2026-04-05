package com.chatflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration REQUEST_WINDOW = Duration.ofMinutes(60);
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(30);
    private static final int MAX_REQUESTS_PER_EMAIL = 5;
    private static final int MAX_REQUESTS_PER_IP = 10;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, LocalStringValue> localStringStore = new ConcurrentHashMap<>();
    private final Map<String, LocalCounterValue> localCounterStore = new ConcurrentHashMap<>();
    private volatile boolean redisFallbackLogged;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String emailOtpKey(String email) {
        return "otp:" + email.toLowerCase().trim();
    }

    private String emailAttemptKey(String email) {
        return "otp_attempts:" + email.toLowerCase().trim();
    }

    private String emailRequestKey(String email) {
        return "otp_requests:email:" + email.toLowerCase().trim();
    }

    private String ipRequestKey(String ip) {
        return "otp_requests:ip:" + ip;
    }

    public synchronized boolean canRequestOtp(String email, String ip) {
        // Rate limiting is temporarily disabled.
        return StringUtils.hasText(email);
    }

    public String generateOtp(String email) {
        String rawOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String key = emailOtpKey(email);
        setValue(key, rawOtp, OTP_TTL);

        // reset verify attempt counter when issuing new OTP
        String attemptsKey = emailAttemptKey(email);
        resetCounter(attemptsKey, ATTEMPT_WINDOW);

        return rawOtp;
    }

    public OtpVerificationResult verifyOtp(String email, String inputOtp) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(inputOtp)) {
            return OtpVerificationResult.invalid("Email and OTP are required");
        }

        String key = emailOtpKey(email);
        String currentOtp = getValue(key);
        if (!StringUtils.hasText(currentOtp)) {
            return OtpVerificationResult.invalid("OTP expired or unavailable");
        }

        String attemptsKey = emailAttemptKey(email);
        long attempts = incrementCounter(attemptsKey, ATTEMPT_WINDOW);

        if (attempts > MAX_VERIFY_ATTEMPTS) {
            return OtpVerificationResult.tooManyAttempts("Maximum verification attempts exceeded");
        }

        if (!currentOtp.equals(inputOtp.trim())) {
            return OtpVerificationResult.invalid(String.format("Invalid OTP. Attempts %d/%d", attempts, MAX_VERIFY_ATTEMPTS));
        }

        deleteValue(key);
        deleteValue(attemptsKey);
        return OtpVerificationResult.success();
    }

    public void clearOtp(String email) {
        if (!StringUtils.hasText(email)) return;
        deleteValue(emailOtpKey(email));
        deleteValue(emailAttemptKey(email));
    }

    private long incrementCounter(String key, Duration ttl) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, ttl);
            }
            return count == null ? 0L : count;
        } catch (RuntimeException ex) {
            logRedisFallback(ex);
            return incrementLocalCounter(key, ttl);
        }
    }

    private void setValue(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            localStringStore.remove(key);
        } catch (RuntimeException ex) {
            logRedisFallback(ex);
            localStringStore.put(key, new LocalStringValue(value, Instant.now().plus(ttl)));
        }
    }

    private String getValue(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return value;
            }
        } catch (RuntimeException ex) {
            logRedisFallback(ex);
        }

        LocalStringValue local = localStringStore.get(key);
        if (local == null) {
            return null;
        }
        if (local.expiresAt.isBefore(Instant.now())) {
            localStringStore.remove(key);
            return null;
        }
        return local.value;
    }

    private void deleteValue(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            logRedisFallback(ex);
        }
        localStringStore.remove(key);
        localCounterStore.remove(key);
    }

    private void resetCounter(String key, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, "0", ttl);
            localCounterStore.remove(key);
        } catch (RuntimeException ex) {
            logRedisFallback(ex);
            localCounterStore.put(key, new LocalCounterValue(0, Instant.now().plus(ttl)));
        }
    }

    private long incrementLocalCounter(String key, Duration ttl) {
        Instant now = Instant.now();
        LocalCounterValue updated = localCounterStore.compute(key, (k, existing) -> {
            if (existing == null || existing.expiresAt.isBefore(now)) {
                return new LocalCounterValue(1, now.plus(ttl));
            }
            existing.value = existing.value + 1;
            return existing;
        });
        return updated == null ? 0L : updated.value;
    }

    private void logRedisFallback(RuntimeException ex) {
        if (!redisFallbackLogged) {
            redisFallbackLogged = true;
            log.warn("Redis unavailable, using in-memory OTP fallback. Reason: {}", ex.getMessage());
            return;
        }
        log.debug("Redis operation failed, in-memory fallback active: {}", ex.getMessage());
    }

    private static class LocalStringValue {
        private final String value;
        private final Instant expiresAt;

        private LocalStringValue(String value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    private static class LocalCounterValue {
        private long value;
        private final Instant expiresAt;

        private LocalCounterValue(long value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    public static class OtpVerificationResult {
        private final boolean success;
        private final boolean tooManyAttempts;
        private final String message;

        private OtpVerificationResult(boolean success, boolean tooManyAttempts, String message) {
            this.success = success;
            this.tooManyAttempts = tooManyAttempts;
            this.message = message;
        }

        public static OtpVerificationResult success() {
            return new OtpVerificationResult(true, false, "OTP verified");
        }

        public static OtpVerificationResult invalid(String message) {
            return new OtpVerificationResult(false, false, message);
        }

        public static OtpVerificationResult tooManyAttempts(String message) {
            return new OtpVerificationResult(false, true, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isTooManyAttempts() {
            return tooManyAttempts;
        }

        public String getMessage() {
            return message;
        }
    }
}
