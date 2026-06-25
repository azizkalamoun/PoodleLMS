package com.enterprise.poodle.dto.response;

import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionResponse {
    private Long id;
    private Long courseId;
    private String title;
    private ContentType contentType;
    private String contentUrl;
    private String fileDescription;
    private Integer orderIndex;
    private QcmType qcmType;
    private Integer maxAttempts;
    private boolean llmDraftEnabled;
    private int questionCount;
}
