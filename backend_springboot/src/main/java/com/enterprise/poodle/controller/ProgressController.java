package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.response.CourseProgressResponse;
import com.enterprise.poodle.dto.response.CourseResponse;
import com.enterprise.poodle.service.CourseService;
import com.enterprise.poodle.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ProgressController {

    private final CourseService courseService;
    private final ProgressService progressService;

    @GetMapping("/courses")
    public ResponseEntity<Page<CourseResponse>> getMyCourses(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(courseService.getEmployeeCourses(pageable));
    }

    @GetMapping("/progress/{courseId}")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId));
    }

    @PostMapping("/progress/{sectionId}/complete")
    public ResponseEntity<Void> markSectionCompleted(@PathVariable Long sectionId) {
        progressService.markSectionCompleted(sectionId);
        return ResponseEntity.noContent().build();
    }
}
