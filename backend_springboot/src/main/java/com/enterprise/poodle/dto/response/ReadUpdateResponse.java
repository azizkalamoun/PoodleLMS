package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ReadUpdateResponse implements Serializable {
    private NotificationResponse notification;
    private long unreadCount;
}
