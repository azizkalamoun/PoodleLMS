package com.enterprise.poodle.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request object for QCM attempt submissions from enrollment endpoints
 * This uses array format instead of Map for better frontend compatibility
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QCMAttemptFromEnrollmentRequest {

    @NotNull(message = "Section ID must not be null")
    private Long sectionId;

    @NotEmpty(message = "Answers must not be empty")
    private List<QCMAnswerRequest> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QCMAnswerRequest {
        @NotNull(message = "Question ID must not be null")
        private Long questionId;

        @NotNull(message = "Selected answer index must not be null")
        private Integer selectedAnswerIndex;
    }
}
