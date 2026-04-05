package com.chatflow.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for request {} {}", request.getMethod(), request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", ex.getMessage(), Instant.now().toString(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ IllegalArgumentException.class, BindException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse error = new ErrorResponse("BAD_REQUEST", ex.getMessage(), Instant.now().toString(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Runtime exception for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());

        ErrorResponse error = new ErrorResponse("RUNTIME_ERROR", ex.getMessage(), Instant.now().toString(),
                request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private String timestamp;
        private String path;

        public ErrorResponse(String errorCode, String message, String timestamp, String path) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getPath() {
            return path;
        }
    }
}
