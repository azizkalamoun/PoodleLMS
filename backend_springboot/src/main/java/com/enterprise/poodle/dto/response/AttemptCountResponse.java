package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttemptCountResponse {
    private long attemptsUsed;
    private Integer maxAttempts; // null means unlimited
    private boolean exhausted;
}
