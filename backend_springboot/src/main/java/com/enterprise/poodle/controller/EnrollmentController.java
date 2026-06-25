package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.QCMAttemptFromEnrollmentRequest;
import com.enterprise.poodle.dto.response.EnrollmentResponse;
import com.enterprise.poodle.dto.response.QCMAttemptResponse;
import com.enterprise.poodle.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Get all enrollments for the current employee
     * An enrollment is created when a course is assigned to an employee's department
     * Admins can also access this to view employee progress
     */
    @GetMapping("/my")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    /**
     * Get all enrollments for a specific employee (admin only)
     * Used by admins to view employee course progress
     *
     * @param employeeId The ID of the employee
     * @return List of enrollments for the specified employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getEmployeeEnrollments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByEmployeeId(employeeId));
    }

    /**
     * Submit a practice QCM attempt for a section in the enrollment
     * Practice attempts don't count towards final grades but show results
     *
     * @param enrollmentId The enrollment ID (used to identify the course/section context)
     * @param request The attempt with section ID and answers
     * @return QCM attempt result with score and correctness feedback
     */
    @PostMapping("/{enrollmentId}/practice-attempt")
    public ResponseEntity<QCMAttemptResponse> submitPracticeAttempt(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody QCMAttemptFromEnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.submitPracticeAttempt(enrollmentId, request));
    }

    /**
     * Submit a final QCM attempt for a section in the enrollment
     * Final attempts count towards grades and may trigger certificate generation
     *
     * @param enrollmentId The enrollment ID (used to identify the course/section context)
     * @param request The attempt with section ID and answers
     * @return QCM attempt result with pass/fail determination
     */
    @PostMapping("/{enrollmentId}/final-attempt")
    public ResponseEntity<QCMAttemptResponse> submitFinalAttempt(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody QCMAttemptFromEnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.submitFinalAttempt(enrollmentId, request));
    }
}
