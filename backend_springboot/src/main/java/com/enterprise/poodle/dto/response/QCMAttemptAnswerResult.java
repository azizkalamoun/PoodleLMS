package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QCMAttemptAnswerResult {
    private Long questionId;
    private Integer selectedAnswerIndex; // 0..3 or -1
    private Integer correctAnswerIndex; // 0..3
    private boolean correct;
}
