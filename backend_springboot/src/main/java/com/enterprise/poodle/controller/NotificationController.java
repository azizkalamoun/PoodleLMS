package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.response.ApiResponse;
import com.enterprise.poodle.dto.response.NotificationResponse;
import com.enterprise.poodle.dto.response.ReadUpdateResponse;
import com.enterprise.poodle.security.SecurityUtils;
import com.enterprise.poodle.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        return ResponseEntity.ok(notificationService.getNotificationsForEmployee(employeeId));
    }

    @GetMapping("/me/paginated")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<NotificationResponse>> getMyNotificationsPaginated(Pageable pageable) {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        return ResponseEntity.ok(notificationService.getNotificationsForEmployee(employeeId, pageable));
    }

    @GetMapping("/me/unread")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<NotificationResponse>> getMyUnreadNotifications() {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForEmployee(employeeId));
    }

    @GetMapping("/me/unread-count")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Long> getUnreadCount() {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        return ResponseEntity.ok(notificationService.getUnreadCountForEmployee(employeeId));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ReadUpdateResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse> markAllAsRead() {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        notificationService.markAllAsRead(employeeId);
        return ResponseEntity.ok(ApiResponse.successMessage("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Notification deleted"));
    }
}
