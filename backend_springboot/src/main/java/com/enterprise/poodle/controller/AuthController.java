package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.LoginRequest;
import com.enterprise.poodle.dto.request.RegisterRequest;
import com.enterprise.poodle.dto.response.AuthResponse;
import com.enterprise.poodle.dto.response.EmployeeResponse;
import com.enterprise.poodle.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info(" [AUTH] Login attempt - Email: {}", request.getEmail());
        try {
            AuthResponse response = authService.login(request);
            log.info(" [AUTH] Login successful - Email: {}, Role: {}", request.getEmail(), response.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(" [AUTH] Login failed - Email: {}, Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info(" [AUTH] Register attempt - Email: {}", request.getEmail());
        try {
            EmployeeResponse response = authService.register(request);
            log.info(" [AUTH] Registration successful - Email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(" [AUTH] Registration failed - Email: {}, Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
