package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.CourseAssignmentRequest;
import com.enterprise.poodle.dto.request.CourseRequest;
import com.enterprise.poodle.dto.response.CourseAssignmentResponse;
import com.enterprise.poodle.dto.response.CourseResponse;
import com.enterprise.poodle.security.SecurityUtils;
import com.enterprise.poodle.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request));
    }

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @RequestParam(value = "order", required = false) String order,
            @PageableDefault(size = 20) Pageable pageable) {
        // Support order=oldest|newest to sort by createdAt
        Pageable pageToUse = pageable;
        if (order != null && !order.isBlank()) {
            Sort sort = "oldest".equalsIgnoreCase(order)
                    ? Sort.by(Sort.Direction.ASC, "createdAt")
                    : Sort.by(Sort.Direction.DESC, "createdAt");
            pageToUse = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        if (securityUtils.isAdmin()) {
            return ResponseEntity.ok(courseService.getAllCourses(pageToUse));
        }
        return ResponseEntity.ok(courseService.getEmployeeCourses(pageToUse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/assign/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseAssignmentResponse> assignCourseToDepartment(
            @PathVariable Long courseId,
            @PathVariable Long departmentId,
            @Valid @RequestBody CourseAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.assignCourseToDepartment(courseId, departmentId, request));
    }

    @GetMapping("/{courseId}/assigned-departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseAssignmentResponse>> getAssignedDepartments(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getAssignedDepartments(courseId));
    }

    @DeleteMapping("/{courseId}/assign/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignCourseFromDepartment(
            @PathVariable Long courseId,
            @PathVariable Long departmentId) {
        courseService.unassignCourseFromDepartment(courseId, departmentId);
        return ResponseEntity.noContent().build();
    }
}
