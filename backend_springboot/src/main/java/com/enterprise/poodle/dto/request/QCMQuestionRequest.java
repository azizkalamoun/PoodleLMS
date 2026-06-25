package com.enterprise.poodle.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QCMQuestionRequest {

    @NotBlank(message = "Question text is required")
    @JsonAlias("text")  // Accept both "questionText" and "text" from frontend
    private String questionText;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    @NotBlank(message = "Correct option is required")
    @Pattern(regexp = "^[A-D]$", message = "Correct option must be A, B, C, or D")
    private String correctOption;
}
