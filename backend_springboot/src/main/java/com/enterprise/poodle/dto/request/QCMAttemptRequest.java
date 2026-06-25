package com.enterprise.poodle.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;

@Data
public class QCMAttemptRequest {

    @NotEmpty(message = "Answers must not be empty")
    private Map<Long, @Pattern(regexp = "^[A-D]$", message = "Answer must be A, B, C, or D") String> answers;
}
