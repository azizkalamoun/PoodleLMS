package com.enterprise.poodle.dto.response;

import com.enterprise.poodle.enums.ProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseProgressResponse {
    private Long courseId;
    private String courseTitle;
    private Integer totalSections;
    private Integer completedSections;
    private double completionPercentage;
    private Integer finalScore;
    private Boolean passed;
    private List<SectionProgressResponse> sectionProgress;

    @Data
    @Builder
    public static class SectionProgressResponse {
        private Long sectionId;
        private String sectionTitle;
        private ProgressStatus status;
        private LocalDateTime completedAt;
    }
}
