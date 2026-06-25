package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;
import com.enterprise.poodle.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean isRead;
    private LocalDateTime readAt;
    private Long relatedEntityId;
    private String relatedEntityType;
}
