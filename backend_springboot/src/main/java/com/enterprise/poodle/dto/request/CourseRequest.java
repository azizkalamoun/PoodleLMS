package com.enterprise.poodle.dto.request;

import com.enterprise.poodle.enums.CourseStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private CourseStatus status;

    @Min(value = 0, message = "Passing score must be at least 0")
    @Max(value = 100, message = "Passing score must not exceed 100")
    private Integer passingScore;

    private List<Long> prerequisiteCourseIds;
}
