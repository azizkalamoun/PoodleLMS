package com.enterprise.poodle.dto.request;

import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SectionRequest {

    @NotBlank(message = "Section title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Content type is required")
    private ContentType contentType;

    private String contentUrl;

    @Size(max = 2000, message = "File description must not exceed 2000 characters")
    private String fileDescription;

    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    private QcmType qcmType;

    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts;

    private Boolean llmDraftEnabled;
}
