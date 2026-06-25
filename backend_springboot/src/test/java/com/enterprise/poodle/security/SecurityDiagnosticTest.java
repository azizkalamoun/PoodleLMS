package com.enterprise.poodle.security;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.LoginRequest;
import com.enterprise.poodle.dto.response.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Diagnostic tests to investigate:
 * 1. Missing/invalid JWT → backend cannot resolve user
 * 2. Null user in service → crash
 * 3. Entity recursion (User ↔ Notification)
 * 4. Database mismatch
 */
@Slf4j
@DisplayName("Security Diagnostic Tests")
class SecurityDiagnosticTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ISSUE 1: Verify JWT generation and validation")
    void testJwtGenerationAndValidation() throws Exception {
        log.info("[TEST] Starting JWT generation test...");
        
        // Step 1: Login and get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("employee@test.com");
        loginRequest.setPassword("Employee@123");

        log.info("[TEST] Sending login request for: {}", loginRequest.getEmail());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        log.info("[TEST] Login response status: {}", loginResult.getResponse().getStatus());
        String loginResponse = loginResult.getResponse().getContentAsString();
        log.info("[TEST] Login response body: {}", loginResponse);

        String token = loginResponse.contains("token") ? 
            objectMapper.readTree(loginResponse).get("token").asText() : null;
        
        assertThat(token).as("JWT token should be generated").isNotNull().isNotEmpty();
        log.info("[TEST] JWT Token generated: {}", token.substring(0, 20) + "...");

        // Step 2: Use token to access protected endpoint
        log.info("[TEST] Using token to access /api/notifications/me/unread-count");
        MvcResult protectedResult = mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        log.info("[TEST] Protected endpoint status: {}", protectedResult.getResponse().getStatus());
        String responseBody = protectedResult.getResponse().getContentAsString();
        log.info("[TEST] Unread count response: {}", responseBody);
    }

    @Test
    @DisplayName("ISSUE 2: Verify SecurityUtils resolves user correctly")
    void testSecurityUtilsUserResolution() throws Exception {
        log.info("[TEST] Starting SecurityUtils user resolution test...");
        
        // Login to get token
        String token = login("employee@test.com", "Employee@123");
        
        // Create a custom test endpoint that logs user resolution details
        log.info("[TEST] Testing /api/employees/me endpoint which uses SecurityUtils");
        MvcResult result = mockMvc.perform(get("/api/employees/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("[TEST] Current employee profile: {}", responseBody);
        
        assertThat(responseBody).contains("email").contains("firstName").contains("lastName");
    }

    @Test
    @DisplayName("ISSUE 3: Check for Entity Recursion - Notification serialization")
    void testNotificationSerializationWithoutRecursion() throws Exception {
        log.info("[TEST] Testing notification serialization for cycles...");
        
        String token = login("employee@test.com", "Employee@123");
        
        log.info("[TEST] Fetching notifications for employee...");
        MvcResult result = mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        int status = result.getResponse().getStatus();
        log.info("[TEST] Notification endpoint status: {}", status);
        
        String responseBody = result.getResponse().getContentAsString();
        log.info("[TEST] Notification response: {}", responseBody);
        
        // Parse response - if there's entity recursion, this will fail with JsonMappingException
        try {
            @SuppressWarnings("unchecked")
            List<NotificationResponse> notifications = objectMapper.readValue(
                responseBody, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, NotificationResponse.class)
            );
            log.info("[TEST] Successfully deserialized {} notifications", notifications.size());
        } catch (Exception e) {
            log.error("[TEST] Failed to deserialize notifications - possible recursion issue", e);
            throw e;
        }
    }

    @Test
    @DisplayName("ISSUE 4: Verify Database Data Integrity")
    void testDatabaseDataIntegrity() throws Exception {
        log.info("[TEST] Checking database integrity...");
        
        String token = login("employee@test.com", "Employee@123");
        
        // Test unread count (should work - simpler query)
        log.info("[TEST] Testing simpler endpoint: /api/notifications/me/unread-count");
        MvcResult unreadResult = mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        
        String unreadCount = unreadResult.getResponse().getContentAsString();
        log.info("[TEST] Unread count works: {}", unreadCount);
        
        // Test full notifications list
        log.info("[TEST] Testing full notifications list: /api/notifications/me");
        MvcResult notifResult = mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
                .andReturn();
        
        int notifStatus = notifResult.getResponse().getStatus();
        String notifBody = notifResult.getResponse().getContentAsString();
        
        log.info("[TEST] Notification list status: {}", notifStatus);
        log.info("[TEST] Response contains: {}", 
            notifBody.substring(0, Math.min(200, notifBody.length())) + "...");
        
        // Log the full error if it failed
        if (notifStatus >= 400) {
            log.error("[TEST] Notification endpoint failed: {}", notifBody);
        }
    }

    @Test
    @DisplayName("COMPREHENSIVE: Full API Flow Test")
    void comprehensiveApiFlowTest() throws Exception {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("COMPREHENSIVE API FLOW TEST - All 4 Issues");
        log.info("═══════════════════════════════════════════════════════════════");
        
        try {
            // 1. Login
            log.info("LOGIN TEST");
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("employee@test.com");
            loginRequest.setPassword("Employee@123");
            
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String loginBody = loginResult.getResponse().getContentAsString();
            String token = objectMapper.readTree(loginBody).get("token").asText();
            log.info("Login successful, token obtained");
            
            // 2. Get employee profile (tests SecurityUtils)
            log.info("EMPLOYEE PROFILE TEST (SecurityUtils)");
            MvcResult profileResult = mockMvc.perform(get("/api/employees/me")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String profileBody = profileResult.getResponse().getContentAsString();
            log.info("Employee profile retrieved: {}", 
                objectMapper.readTree(profileBody).get("email"));
            
            // 3. Get unread notifications count
            log.info("UNREAD COUNT TEST");
            MvcResult unreadResult = mockMvc.perform(get("/api/notifications/me/unread-count")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String unreadBody = unreadResult.getResponse().getContentAsString();
            log.info("Unread count: {}", unreadBody);
            
            // 4. Get full notification list
            log.info("FULL NOTIFICATIONS LIST TEST");
            MvcResult notifResult = mockMvc.perform(get("/api/notifications/me")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();
            
            int notifStatus = notifResult.getResponse().getStatus();
            String notifBody = notifResult.getResponse().getContentAsString();
            
            log.info("Notification endpoint status: {}", notifStatus);
            
            if (notifStatus == 200) {
                log.info("Notifications retrieved successfully");
            } else {
                log.error("FAILED - Status: {}", notifStatus);
                log.error("Error response: {}", notifBody);
            }
            
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("COMPREHENSIVE TEST COMPLETE");
            log.info("═══════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            log.error("TEST FAILED WITH EXCEPTION", e);
            throw e;
        }
    }
}
