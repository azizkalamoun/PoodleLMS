package com.enterprise.poodle.service;

import com.enterprise.poodle.BaseIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the LazyInitializationException fix for NotificationService
 */
@Slf4j
@DisplayName("NotificationService - Lazy Initialization Fix Tests")
class NotificationServiceLazyInitTest extends BaseIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("Should fetch notifications without LazyInitializationException")
    void testGetNotificationsWithLazyInitFix() throws Exception {
        String token = login("employee@test.com", "Employee@123");
        
        log.info("Testing /api/notifications/me endpoint");
        
        var result = mockMvc.perform(get("/api/notifications/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        log.info("Response received: {}", responseBody);
        
        assertThat(responseBody).isNotEmpty();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should fetch unread notifications without LazyInitializationException")
    void testGetUnreadNotificationsWithLazyInitFix() throws Exception {
        String token = login("employee@test.com", "Employee@123");
        
        log.info("Testing /api/notifications/me/unread endpoint");
        
        var result = mockMvc.perform(get("/api/notifications/me/unread")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        log.info("Unread notifications response: {}", responseBody);
        
        assertThat(responseBody).isNotEmpty();
    }

    @Test
    @DisplayName("Should fetch notification count without LazyInitializationException")
    void testGetUnreadCountWithLazyInitFix() throws Exception {
        String token = login("employee@test.com", "Employee@123");
        
        log.info("Testing /api/notifications/me/unread-count endpoint");
        
        var result = mockMvc.perform(get("/api/notifications/me/unread-count")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        log.info("Unread count: {}", responseBody);
        
        assertThat(responseBody).isNotEmpty();
        assertThat(Integer.parseInt(responseBody)).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Direct service call - Verify lazy initialization in transaction")
    void testServiceDirectCallWithinTransaction() {
        log.info("Testing NotificationService.getNotificationsForEmployee() directly");
        
        // Within test transaction context
        var notifications = notificationService.getNotificationsForEmployee(1L);
        
        log.info("Retrieved {} notifications without exception", notifications.size());
        
        // Verify notifications have all required fields
        notifications.forEach(notif -> {
            assertThat(notif).isNotNull();
            assertThat(notif.getTitle()).isNotEmpty();
            assertThat(notif.getType()).isNotNull();
            log.info("  - Notification: {} (type: {})", notif.getTitle(), notif.getType());
        });
    }
}
