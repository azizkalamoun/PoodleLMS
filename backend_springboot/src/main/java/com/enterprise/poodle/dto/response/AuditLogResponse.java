package com.enterprise.poodle.dto.response;

import com.enterprise.poodle.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private ActionType actionType;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
