package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QCMQuestionResponse {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // Only returned to admins
    private boolean llmGenerated;
}
