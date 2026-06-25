package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.response.NotificationResponse;
import com.enterprise.poodle.dto.response.ReadUpdateResponse;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.entity.EmployeeNotification;
import com.enterprise.poodle.entity.Notification;
import com.enterprise.poodle.enums.NotificationType;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.EmployeeNotificationRepository;
import com.enterprise.poodle.repository.EmployeeRepository;
import com.enterprise.poodle.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeNotificationRepository employeeNotificationRepository;
    private final EmployeeRepository employeeRepository;
    private final EntityManager entityManager;
 
    @Transactional
    public NotificationResponse notifyEmployee(Long employeeId, String title, String message, NotificationType type) {
        return notifyEmployee(employeeId, title, message, type, null, null);
    }

    @Transactional
    public NotificationResponse notifyEmployee(Long employeeId, String title, String message, NotificationType type, 
                                              Long relatedEntityId, String relatedEntityType) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .createdAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);

        EmployeeNotification employeeNotification = EmployeeNotification.builder()
                .employee(employee)
                .notification(notification)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        employeeNotificationRepository.save(employeeNotification);

        log.info("Notification created for employee {} with title: {}", employeeId, title);

        return mapToResponse(employeeNotification);
    }

    @Transactional
    public void notifyMultipleEmployees(List<Long> employeeIds, String title, String message, NotificationType type) {
        notifyMultipleEmployees(employeeIds, title, message, type, null, null);
    }

    @Transactional
    public void notifyMultipleEmployees(List<Long> employeeIds, String title, String message, NotificationType type,
                                        Long relatedEntityId, String relatedEntityType) {
        // Create single notification for all employees
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .createdAt(LocalDateTime.now())
                .build();

        final Notification savedNotification = notificationRepository.save(notification);

        // Create employee_notification records for each employee
        List<EmployeeNotification> employeeNotifications = employeeIds.stream()
                .map(employeeId -> {
                    Employee employee = employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

                    return EmployeeNotification.builder()
                            .employee(employee)
                            .notification(savedNotification)
                            .read(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        employeeNotificationRepository.saveAll(employeeNotifications);

        log.info("Notification sent to {} employees with title: {}", employeeIds.size(), title);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForEmployee(Long employeeId) {
        List<EmployeeNotification> notifications = 
            employeeNotificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        
        // Initialize lazy properties WHILE SESSION IS OPEN to prevent LazyInitializationException
        notifications.forEach(en -> {
            if (en != null && en.getNotification() != null) {
                try {
                    en.getNotification().getTitle();  // Force load title
                    en.getNotification().getType();   // Force load type - critical for enum
                    en.getNotification().getMessage(); // Force load message
                } catch (Exception e) {
                    log.warn("Failed to initialize notification {} for employee notification {}: {}", 
                            en.getNotification().getId(), en.getId(), e.getMessage());
                    // Set notification to null to skip it in mapping
                    en.setNotification(null);
                }
            }
        });
        
        return notifications.stream()
                .map(this::mapToResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForEmployee(Long employeeId, Pageable pageable) {
        Page<EmployeeNotification> notifications = 
            employeeNotificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
        
        // Initialize lazy properties WHILE SESSION IS OPEN
        notifications.forEach(en -> {
            if (en != null && en.getNotification() != null) {
                try {
                    en.getNotification().getTitle();
                    en.getNotification().getType();
                    en.getNotification().getMessage();
                } catch (Exception e) {
                    log.warn("Failed to initialize notification {} for employee notification {}: {}", 
                            en.getNotification().getId(), en.getId(), e.getMessage());
                    // Set notification to null to skip it in mapping
                    en.setNotification(null);
                }
            }
        });
        
        return notifications.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsForEmployee(Long employeeId) {
        List<EmployeeNotification> unreadNotifications = 
            employeeNotificationRepository.findByEmployeeIdAndReadFalseOrderByCreatedAtDesc(employeeId);
        
        // Initialize lazy properties WHILE SESSION IS OPEN
        unreadNotifications.forEach(en -> {
            if (en != null && en.getNotification() != null) {
                try {
                    en.getNotification().getTitle();
                    en.getNotification().getType();
                    en.getNotification().getMessage();
                } catch (Exception e) {
                    log.warn("Failed to initialize notification {} for employee notification {}: {}", 
                            en.getNotification().getId(), en.getId(), e.getMessage());
                    // Set notification to null to skip it in mapping
                    en.setNotification(null);
                }
            }
        });
        
        return unreadNotifications.stream()
                .map(this::mapToResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    public long getUnreadCountForEmployee(Long employeeId) {
    return employeeNotificationRepository.countByEmployeeIdAndReadFalse(employeeId);
    }

    @Transactional
    public ReadUpdateResponse markAsRead(Long notificationId) {
        // First get the current state for response
        EmployeeNotification employeeNotification = employeeNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Use the repository's update query to ensure the database is updated
        employeeNotificationRepository.markAsRead(notificationId);

        // Refresh the entity to get updated state. Use EntityManager.refresh to force reloading
        try {
            entityManager.refresh(employeeNotification);
        } catch (Exception ex) {
            // If refresh fails for any reason, fall back to reloading from repository
            log.debug("EntityManager.refresh failed for EmployeeNotification {}: {}. Falling back to findById.", notificationId, ex.getMessage());
            employeeNotification = employeeNotificationRepository.findById(notificationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found after update with id: " + notificationId));
        }

        log.info("Notification {} marked as read", notificationId);

        // Compute updated unread count for the employee
        Long employeeId = employeeNotification.getEmployee() != null ? employeeNotification.getEmployee().getId() : null;
        long unreadCount = 0L;
        if (employeeId != null) {
            unreadCount = employeeNotificationRepository.countByEmployeeIdAndReadFalse(employeeId);
        }

        return ReadUpdateResponse.builder()
                .notification(mapToResponse(employeeNotification))
                .unreadCount(unreadCount)
                .build();
    }

    @Transactional
    public void markAllAsRead(Long employeeId) {
        employeeNotificationRepository.markAllAsRead(employeeId);
        log.info("All notifications marked as read for employee {}", employeeId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        employeeNotificationRepository.deleteById(notificationId);
        log.info("Notification {} deleted", notificationId);
    }

    private NotificationResponse mapToResponse(EmployeeNotification employeeNotification) {
        if (employeeNotification == null) {
            log.warn("EmployeeNotification is null in mapToResponse");
            return null;
        }

        Notification notification = employeeNotification.getNotification();
        if (notification == null) {
            log.warn("Notification object is null for EmployeeNotification id: {}", employeeNotification.getId());
            return null;
        }

        log.debug("Mapping EmployeeNotification id: {}, read: {}", employeeNotification.getId(), employeeNotification.isRead());

        try {
            return NotificationResponse.builder()
                    .id(employeeNotification.getId())
                    .title(notification.getTitle() != null ? notification.getTitle() : "")
                    .message(notification.getMessage() != null ? notification.getMessage() : "")
                    .type(notification.getType())
                    .createdAt(notification.getCreatedAt())
                    .isRead(employeeNotification.isRead())
                    .readAt(employeeNotification.getReadAt())
                    .relatedEntityId(notification.getRelatedEntityId())
                    .relatedEntityType(notification.getRelatedEntityType())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping EmployeeNotification to NotificationResponse for id: {}",
                    employeeNotification.getId(), e);
            throw e;
        }
    }
}
