package com.enterprise.poodle.dto.response;

import com.enterprise.poodle.enums.CourseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private CourseStatus status;
    private Integer passingScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> prerequisiteCourseIds;
    private Integer publishNotifiedCount;
}
