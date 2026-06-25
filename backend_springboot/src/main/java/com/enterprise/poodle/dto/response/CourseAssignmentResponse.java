package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CourseAssignmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long departmentId;
    private String departmentName;
    private LocalDate deadlineDate;
    private Integer notifiedCount;
}
