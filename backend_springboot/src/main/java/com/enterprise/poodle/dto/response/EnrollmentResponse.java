package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long courseId;
    private String courseTitle;
    private String status; // IN_PROGRESS, PASSED, FAILED, OVERDUE
    private Double score;
    private Integer attempts;
    private LocalDate deadline;
    private LocalDateTime updatedAt;
}
