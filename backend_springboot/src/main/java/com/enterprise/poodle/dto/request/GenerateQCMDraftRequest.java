package com.enterprise.poodle.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerateQCMDraftRequest {

    @Min(value = 1, message = "Number of questions must be at least 1")
    @Max(value = 50, message = "Number of questions cannot exceed 50")
    private Integer numberOfQuestions;

    public Integer getNumberOfQuestions() {
        return numberOfQuestions != null ? numberOfQuestions : 5; // Default to 5
    }
}
