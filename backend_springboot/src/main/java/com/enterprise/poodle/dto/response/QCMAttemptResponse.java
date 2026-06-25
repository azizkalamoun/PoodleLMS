package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QCMAttemptResponse {
    private Long id;
    private Long sectionId;
    private Integer attemptNumber;
    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private long attemptsUsed;
    private Integer maxAttempts; // null = unlimited
    private boolean exhausted;   // true when attemptsUsed >= maxAttempts
    private boolean passed;
    private LocalDateTime takenAt;
    private List<QCMAttemptAnswerResult> answers;
}
