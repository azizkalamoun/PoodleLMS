package com.enterprise.poodle.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CourseAssignmentRequest {

    private LocalDate deadlineDate;
}
